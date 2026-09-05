package com.xaiworld.expensetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import com.xaiworld.expensetracker.data.User

/**
 * Shown once per phone (and reachable later from the top bar) to pick which of the two
 * people is using this device. That choice drives the default "paid by" value and the name
 * this phone shows up as to the other one during sync.
 */
@Composable
fun UserPickerDialog(
    current: User?,
    dismissible: Boolean,
    onPick: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        title = { Text("Which phone is this?") },
        text = {
            Column {
                Text("Pick who uses this phone. The other phone should pick the other person.")
                User.entries.forEach { user ->
                    Row {
                        RadioButton(selected = current == user, onClick = { onPick(user) })
                        Text(user.displayName)
                    }
                }
            }
        },
        confirmButton = {
            if (dismissible) {
                TextButton(onClick = onDismiss) { Text("Done") }
            }
        }
    )
}
