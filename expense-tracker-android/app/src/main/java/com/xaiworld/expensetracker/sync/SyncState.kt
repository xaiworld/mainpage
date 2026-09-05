package com.xaiworld.expensetracker.sync

sealed interface SyncState {
    data object Idle : SyncState
    data object Searching : SyncState
    data class FoundDevice(val name: String) : SyncState
    data class Connected(val name: String) : SyncState
    data class Success(val receivedCount: Int) : SyncState
    data class Error(val message: String) : SyncState
}
