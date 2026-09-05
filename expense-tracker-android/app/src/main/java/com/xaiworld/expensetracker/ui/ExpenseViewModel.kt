package com.xaiworld.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xaiworld.expensetracker.data.AppDatabase
import com.xaiworld.expensetracker.data.Balance
import com.xaiworld.expensetracker.data.Expense
import com.xaiworld.expensetracker.data.ExpenseRepository
import com.xaiworld.expensetracker.data.Prefs
import com.xaiworld.expensetracker.data.User
import com.xaiworld.expensetracker.sync.NearbySyncManager
import com.xaiworld.expensetracker.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(AppDatabase.getInstance(application).expenseDao())
    private val prefs = Prefs(application)

    private val syncManager = NearbySyncManager(
        context = application,
        getLocalExpenses = { repository.getAllForSync() },
        onExpensesReceived = { received -> repository.mergeIncoming(received) }
    )

    val expenses: StateFlow<List<Expense>> = repository.observeVisible()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balance: StateFlow<Balance> = expenses
        .map { repository.balanceOf(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Balance(0.0))

    val syncState: StateFlow<SyncState> = syncManager.state

    private val _localUser = MutableStateFlow(prefs.localUser)
    val localUser: StateFlow<User?> = _localUser

    fun setLocalUser(user: User) {
        prefs.localUser = user
        _localUser.value = user
    }

    fun addExpense(amount: Double, date: Long, paidBy: User, note: String, xaiSharePercent: Int) {
        viewModelScope.launch {
            repository.add(amount, date, paidBy, note, xaiSharePercent)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.delete(expense)
        }
    }

    fun startSync() {
        val name = _localUser.value?.displayName ?: "Phone"
        syncManager.startSync(name)
    }

    fun stopSync() {
        syncManager.stopSync()
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.stopSync()
    }
}
