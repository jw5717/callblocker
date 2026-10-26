package com.jw.callblocker.data

import android.content.Context
import android.content.SharedPreferences
import android.telephony.PhoneNumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** A single blocked call, as shown in the log screen. */
data class BlockedCall(
    val number: String?,
    val timestamp: Long
) {
    val isUnknown: Boolean get() = number.isNullOrBlank()
    val displayNumber: String get() = if (isUnknown) UNKNOWN_LABEL else number!!

    companion object {
        const val UNKNOWN_LABEL = "Unknown / withheld"
    }
}

/**
 * Single source of truth for app state, backed entirely by on-device
 * SharedPreferences. Nothing here ever touches the network — the app holds no
 * INTERNET permission, so call data physically cannot leave the device.
 *
 * The call-screening service and the UI run in the same process, so they share
 * this one singleton instance; writes from the service are immediately visible
 * to the UI through the exposed [StateFlow]s.
 */
class CallStore private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _enabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _whitelist = MutableStateFlow(readWhitelist())
    val whitelist: StateFlow<Set<String>> = _whitelist.asStateFlow()

    private val _blockedCalls = MutableStateFlow(readLog())
    val blockedCalls: StateFlow<List<BlockedCall>> = _blockedCalls.asStateFlow()

    // ----- master on/off toggle -----

    /** Read directly (used by the service on each incoming call). */
    val isEnabled: Boolean get() = prefs.getBoolean(KEY_ENABLED, true)

    fun setEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        _enabled.value = value
    }

    // ----- "notify when a call is blocked" toggle (default off) -----

    private val _notifyOnBlock = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY, false))
    val notifyOnBlock: StateFlow<Boolean> = _notifyOnBlock.asStateFlow()

    /** Read directly (used by the service when it blocks a call). */
    val isNotifyOnBlock: Boolean get() = prefs.getBoolean(KEY_NOTIFY, false)

    fun setNotifyOnBlock(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY, value).apply()
        _notifyOnBlock.value = value
    }

    // ----- whitelist -----

    /** True if [number] has been explicitly allowed through, independent of contacts. */
    fun isWhitelisted(number: String?): Boolean {
        if (number.isNullOrBlank()) return false
        val set = prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
        return set.any { it == number || PhoneNumberUtils.compare(it, number) }
    }

    fun addToWhitelist(number: String) {
        if (number.isBlank()) return
        val current = currentWhitelist()
        current.add(number)
        commitWhitelist(current)
    }

    fun removeFromWhitelist(number: String) {
        val current = currentWhitelist()
        current.remove(number)
        commitWhitelist(current)
    }

    private fun currentWhitelist(): MutableSet<String> =
        (prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()).toMutableSet()

    private fun commitWhitelist(set: Set<String>) {
        // Store a copy — SharedPreferences must not be handed a mutated set instance.
        prefs.edit().putStringSet(KEY_WHITELIST, HashSet(set)).apply()
        _whitelist.value = set.toSet()
    }

    private fun readWhitelist(): Set<String> =
        (prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()).toSet()

    // ----- blocked-call log -----

    fun logBlockedCall(number: String?) {
        val updated = ArrayList<BlockedCall>(readLog())
        updated.add(0, BlockedCall(number, System.currentTimeMillis()))
        while (updated.size > MAX_LOG) updated.removeAt(updated.size - 1)
        writeLog(updated)
        _blockedCalls.value = updated
    }

    fun clearLog() {
        prefs.edit().remove(KEY_LOG).apply()
        _blockedCalls.value = emptyList()
    }

    private fun readLog(): List<BlockedCall> {
        val raw = prefs.getString(KEY_LOG, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BlockedCall(
                    number = if (o.isNull("number")) null else o.getString("number"),
                    timestamp = o.getLong("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeLog(list: List<BlockedCall>) {
        val arr = JSONArray()
        list.forEach { c ->
            arr.put(JSONObject().apply {
                put("number", c.number ?: JSONObject.NULL)
                put("timestamp", c.timestamp)
            })
        }
        prefs.edit().putString(KEY_LOG, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "call_blocker_prefs"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_NOTIFY = "notify_on_block"
        private const val KEY_WHITELIST = "whitelist"
        private const val KEY_LOG = "blocked_log"
        private const val MAX_LOG = 500

        @Volatile
        private var INSTANCE: CallStore? = null

        fun get(context: Context): CallStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CallStore(context).also { INSTANCE = it }
            }
    }
}