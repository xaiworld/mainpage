package com.xaiworld.expensetracker.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    /** Live, display-ready list: newest first, tombstones hidden. */
    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC, lastModified DESC")
    fun observeVisible(): Flow<List<Expense>>

    /** Everything, tombstones included — this is what gets sent to the other phone on sync. */
    @Query("SELECT * FROM expenses")
    suspend fun getAllForSync(): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: String): Expense?

    @Upsert
    suspend fun upsert(expense: Expense)

    @Upsert
    suspend fun upsertAll(expenses: List<Expense>)

    @Query("UPDATE expenses SET isDeleted = 1, lastModified = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}
