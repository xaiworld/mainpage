package com.xaiworld.expensetracker.data

import kotlinx.coroutines.flow.Flow

/** Net balance between the two of them, derived from every non-deleted expense. */
data class Balance(val xaiOwesNat: Double) {
    val isSettled: Boolean get() = kotlin.math.abs(xaiOwesNat) < 0.005
    val owedBy: User? get() = when {
        isSettled -> null
        xaiOwesNat > 0 -> User.XAI
        else -> User.NAT
    }
    val amount: Double get() = kotlin.math.abs(xaiOwesNat)
}

class ExpenseRepository(private val dao: ExpenseDao) {

    fun observeVisible(): Flow<List<Expense>> = dao.observeVisible()

    suspend fun add(
        amount: Double,
        date: Long,
        paidBy: User,
        note: String,
        xaiSharePercent: Int
    ) {
        dao.upsert(
            Expense(
                amount = amount,
                date = date,
                paidBy = paidBy,
                note = note,
                xaiSharePercent = xaiSharePercent
            )
        )
    }

    suspend fun delete(expense: Expense) {
        dao.markDeleted(expense.id, System.currentTimeMillis())
    }

    fun balanceOf(expenses: List<Expense>): Balance =
        Balance(expenses.sumOf { it.xaiOwesNat })

    /** Full local state (tombstones included) to hand to the other phone during a sync. */
    suspend fun getAllForSync(): List<Expense> = dao.getAllForSync()

    /**
     * Merges expenses received from the other phone: last-write-wins per expense id.
     * A tombstone (isDeleted = true) is merged in exactly like any other update, which is
     * how a deletion made on one phone propagates to the other.
     */
    suspend fun mergeIncoming(incoming: List<Expense>): Int {
        var applied = 0
        for (remote in incoming) {
            val local = dao.getById(remote.id)
            if (local == null || remote.lastModified > local.lastModified) {
                dao.upsert(remote)
                applied++
            }
        }
        return applied
    }
}
