package com.jw.callblocker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jw.callblocker.data.BlockedCall
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hasContacts: Boolean,
    hasRole: Boolean,
    enabledState: State<Boolean>,
    notifyOnBlockState: State<Boolean>,
    blockedCountState: State<Int>,
    onEnableSetup: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onToggleNotify: (Boolean) -> Unit,
    onOpenLog: () -> Unit
) {
    val enabled by enabledState
    val notifyOnBlock by notifyOnBlockState
    val blockedCount by blockedCountState
    val fullySetUp = hasContacts && hasRole

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Call Blocker", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        val statusText = when {
            !fullySetUp && !hasContacts ->
                "Step 1: grant Contacts permission so the app can tell your contacts apart from strangers."
            !fullySetUp ->
                "Step 2: set this app as your call-screening app."
            enabled ->
                "Active. Only your contacts and whitelisted numbers can ring through. Everything else is blocked silently."
            else ->
                "Blocking is turned OFF. All calls ring through normally."
        }
        Text(
            statusText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!fullySetUp) {
            Button(onClick = onEnableSetup) {
                Text(if (!hasContacts) "Grant Contacts access" else "Set as call-screening app")
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Block non-contacts", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Reject calls that aren't from a contact or whitelisted number.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = onToggleEnabled)
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notify when a call is blocked", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Show a notification in the status bar each time a call is blocked.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = notifyOnBlock, onCheckedChange = onToggleNotify)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onOpenLog) {
            Text("View blocked calls ($blockedCount)")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    blockedCallsState: State<List<BlockedCall>>,
    whitelistState: State<Set<String>>,
    onWhitelistChange: (String, Boolean) -> Unit,
    onClearLog: () -> Unit,
    onBack: () -> Unit
) {
    val blockedCalls by blockedCallsState
    val whitelist by whitelistState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked calls") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (blockedCalls.isNotEmpty()) {
                        TextButton(onClick = onClearLog) { Text("Clear") }
                    }
                }
            )
        }
    ) { padding ->
        if (blockedCalls.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No calls have been blocked yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            val today = remember { LocalDate.now() }
            // Group newest-first calls into day sections, preserving order.
            val sections = remember(blockedCalls) {
                blockedCalls.groupBy { toLocalDate(it.timestamp) }.entries.toList()
            }

            LazyColumn(modifier = Modifier.padding(padding)) {
                item { ColumnHeaderRow() }

                sections.forEach { (date, calls) ->
                    item { DaySectionHeader(dayLabel(date, today)) }
                    items(calls) { call ->
                        BlockedCallRow(
                            call = call,
                            checked = !call.isUnknown && call.number in whitelist,
                            onCheckedChange = { checked ->
                                call.number?.let { onWhitelistChange(it, checked) }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

/** Column titles: "Blocked number" on the left, "Whitelist" on the right. */
@Composable
private fun ColumnHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Blocked number",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text("Whitelist", style = MaterialTheme.typography.labelLarge)
    }
    HorizontalDivider()
}

@Composable
private fun DaySectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun BlockedCallRow(
    call: BlockedCall,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val rowModifier = if (call.isUnknown) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    }
    Row(
        modifier = rowModifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                call.displayNumber,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(formatTime(call.timestamp), style = MaterialTheme.typography.bodySmall)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            enabled = !call.isUnknown
        )
    }
}

private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dateFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())

private fun toLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

private fun formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormat)

private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(dateFormat)
}