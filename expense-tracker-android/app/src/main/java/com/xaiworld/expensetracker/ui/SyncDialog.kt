package com.xaiworld.expensetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xaiworld.expensetracker.sync.SyncState

@Composable
fun SyncDialog(
    state: SyncState,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync with the other phone") },
        text = {
            Column {
                Text(
                    "Make sure Bluetooth and Wi-Fi are turned on and both phones are near " +
                        "each other, then start syncing on both phones."
                )
                Text(text = statusText(state), modifier = Modifier.padding(top = 16.dp))
                if (state is SyncState.Searching || state is SyncState.FoundDevice) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onStart) { Text(if (state == SyncState.Idle) "Start sync" else "Retry") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun statusText(state: SyncState): String = when (state) {
    is SyncState.Idle -> "Not syncing."
    is SyncState.Searching -> "Looking for the other phone…"
    is SyncState.FoundDevice -> "Found ${state.name}, connecting…"
    is SyncState.Connected -> "Connected, exchanging expenses…"
    is SyncState.Success -> if (state.receivedCount > 0) {
        "Done! Received ${state.receivedCount} update(s) from the other phone."
    } else {
        "Done! Already up to date."
    }
    is SyncState.Error -> "Couldn't sync: ${state.message}"
}
