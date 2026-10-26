package com.jw.callblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.jw.callblocker.data.CallStore
import com.jw.callblocker.ui.HomeScreen
import com.jw.callblocker.ui.LogScreen
import com.jw.callblocker.ui.theme.CallBlockerTheme

private enum class Screen { HOME, LOG }

class MainActivity : ComponentActivity() {

    private var refreshStatus: (() -> Unit)? = null

    private val requestContactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestCallScreeningRole()
        refreshStatus?.invoke()
    }

    private val requestRole = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshStatus?.invoke()
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* If denied, blocking still works; the notification just won't appear. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = CallStore.get(this)
        setContent {
            CallBlockerTheme {
                var screen by remember { mutableStateOf(Screen.HOME) }
                var hasContacts by remember { mutableStateOf(hasContactsPermission()) }
                var hasRole by remember { mutableStateOf(hasCallScreeningRole()) }
                val enabledState = store.enabled.collectAsState()
                val notifyOnBlockState = store.notifyOnBlock.collectAsState()
                val blockedCalls = store.blockedCalls.collectAsState()
                val whitelist = store.whitelist.collectAsState()
                val blockedCount: State<Int> =
                    remember { derivedStateOf { blockedCalls.value.size } }

                registerRefresh {
                    hasContacts = hasContactsPermission()
                    hasRole = hasCallScreeningRole()
                }

                when (screen) {
                    Screen.HOME -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            hasContacts = hasContacts,
                            hasRole = hasRole,
                            enabledState = enabledState,
                            notifyOnBlockState = notifyOnBlockState,
                            blockedCountState = blockedCount,
                            onEnableSetup = { onEnableClicked() },
                            onToggleEnabled = { store.setEnabled(it) },
                            onToggleNotify = { onToggleNotify(store, it) },
                            onOpenLog = { screen = Screen.LOG }
                        )
                    }

                    Screen.LOG -> {
                        BackHandler { screen = Screen.HOME }
                        LogScreen(
                            blockedCallsState = blockedCalls,
                            whitelistState = whitelist,
                            onWhitelistChange = { number, add ->
                                if (add) store.addToWhitelist(number)
                                else store.removeFromWhitelist(number)
                            },
                            onClearLog = { store.clearLog() },
                            onBack = { screen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus?.invoke()
    }

    private fun registerRefresh(block: () -> Unit) {
        refreshStatus = block
    }

    private fun onToggleNotify(store: CallStore, enabled: Boolean) {
        store.setNotifyOnBlock(enabled)
        // On Android 13+ we need POST_NOTIFICATIONS before notifications can show.
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun onEnableClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        } else {
            requestCallScreeningRole()
        }
    }

    private fun requestCallScreeningRole() {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        ) {
            requestRole.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }

    fun hasContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    fun hasCallScreeningRole(): Boolean =
        (getSystemService(Context.ROLE_SERVICE) as RoleManager).isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}