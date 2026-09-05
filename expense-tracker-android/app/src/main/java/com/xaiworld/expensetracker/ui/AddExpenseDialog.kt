package com.xaiworld.expensetracker.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xaiworld.expensetracker.data.SplitPreset
import com.xaiworld.expensetracker.data.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    defaultPayer: User,
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: Long, paidBy: User, note: String, xaiSharePercent: Int) -> Unit
) {
    val context = LocalContext.current
    var amountText by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableStateOf(startOfToday()) }
    var paidBy by rememberSaveable { mutableStateOf(defaultPayer) }
    var xaiSharePercent by rememberSaveable { mutableStateOf(50) }
    var customSplit by rememberSaveable { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull()
    val isValid = amount != null && amount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add expense") },
        text = {
            Column(modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(dateLabel(dateMillis))
                    TextButton(onClick = { pickDate(context, dateMillis) { dateMillis = it } }) {
                        Text("Change date")
                    }
                }

                Text("Paid by", modifier = Modifier.padding(top = 12.dp))
                Row {
                    User.entries.forEach { user ->
                        Row {
                            RadioButton(selected = paidBy == user, onClick = { paidBy = user })
                            Text(user.displayName, modifier = Modifier.padding(end = 12.dp))
                        }
                    }
                }

                Text("Split", modifier = Modifier.padding(top = 12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SplitPreset.entries.toList()) { preset ->
                        FilterChip(
                            selected = !customSplit && xaiSharePercent == preset.xaiSharePercent,
                            onClick = {
                                customSplit = false
                                xaiSharePercent = preset.xaiSharePercent
                            },
                            label = { Text(preset.label) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = customSplit,
                            onClick = { customSplit = true },
                            label = { Text("Custom") }
                        )
                    }
                }
                if (customSplit) {
                    Text("Xai $xaiSharePercent% / Nat ${100 - xaiSharePercent}%")
                    Slider(
                        value = xaiSharePercent.toFloat(),
                        onValueChange = { xaiSharePercent = it.toInt() },
                        valueRange = 0f..100f,
                        steps = 19
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onSave(amount!!, dateMillis, paidBy, noteText.trim(), xaiSharePercent) }
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun dateLabel(millis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(java.util.Date(millis))

private fun pickDate(context: Context, currentMillis: Long, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentMillis }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val picked = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onPicked(picked.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
