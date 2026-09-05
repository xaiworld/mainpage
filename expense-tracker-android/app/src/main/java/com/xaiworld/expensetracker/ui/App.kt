package com.xaiworld.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xaiworld.expensetracker.data.Balance
import com.xaiworld.expensetracker.data.Expense
import com.xaiworld.expensetracker.data.User
import com.xaiworld.expensetracker.export.WhatsAppExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseApp(
    viewModel: ExpenseViewModel,
    onRequestSync: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val localUser by viewModel.localUser.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showUserPicker by remember { mutableStateOf(false) }

    LaunchedEffect(localUser) {
        if (localUser == null) showUserPicker = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xai & Nat") },
                actions = {
                    IconButton(onClick = { showSyncDialog = true }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync with other phone")
                    }
                    IconButton(onClick = { WhatsAppExporter.shareToWhatsApp(context, expenses, balance) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share to WhatsApp")
                    }
                    IconButton(onClick = { showUserPicker = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Who is using this phone")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BalanceCard(balance = balance, modifier = Modifier.padding(16.dp))
            if (expenses.isEmpty()) {
                Text(
                    "No expenses yet. Tap + to add the first one.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseRow(
                            expense = expense,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            defaultPayer = localUser ?: User.XAI,
            onDismiss = { showAddDialog = false },
            onSave = { amount, date, paidBy, note, xaiSharePercent ->
                viewModel.addExpense(amount, date, paidBy, note, xaiSharePercent)
                showAddDialog = false
            }
        )
    }

    if (showSyncDialog) {
        SyncDialog(
            state = syncState,
            onStart = onRequestSync,
            onDismiss = {
                showSyncDialog = false
                viewModel.stopSync()
            }
        )
    }

    if (showUserPicker) {
        UserPickerDialog(
            current = localUser,
            dismissible = localUser != null,
            onPick = { viewModel.setLocalUser(it) },
            onDismiss = { showUserPicker = false }
        )
    }
}

@Composable
private fun BalanceCard(balance: Balance, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Balance", style = MaterialTheme.typography.titleMedium)
            val text = when (val owedBy = balance.owedBy) {
                null -> "You're all settled up!"
                User.XAI -> "Xai owes Nat ${formatAmount(balance.amount)}"
                User.NAT -> "Nat owes Xai ${formatAmount(balance.amount)}"
            }
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(formatAmount(expense.amount), style = MaterialTheme.typography.titleMedium)
                Text(dateFormatter.format(Date(expense.date)), style = MaterialTheme.typography.bodySmall)
                Text(
                    "Paid by ${expense.paidBy.displayName} · split ${expense.xaiSharePercent}/${expense.natSharePercent}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (expense.note.isNotBlank()) {
                    Text(expense.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete expense")
            }
        }
    }
}

private val dateFormatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

private fun formatAmount(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)
