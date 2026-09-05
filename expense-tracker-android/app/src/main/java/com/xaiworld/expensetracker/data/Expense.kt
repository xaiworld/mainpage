package com.xaiworld.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A single shared expense.
 *
 * Rows are never hard-deleted locally: [isDeleted] is a tombstone so that a deletion made on
 * one phone also removes the expense on the other phone the next time they sync (see
 * [com.xaiworld.expensetracker.sync.NearbySyncManager]). [lastModified] is the clock used to
 * resolve conflicts: whichever copy of a given [id] was touched most recently wins the merge.
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    /** Total amount of the expense, in whatever currency the couple uses. */
    val amount: Double,
    /** Day the expense happened, as epoch millis (midnight, local time). */
    val date: Long,
    /** Which of the two people actually paid the bill. */
    val paidBy: User,
    val note: String = "",
    /**
     * How the cost is divided between the two of them, as Xai's share out of 100.
     * Nat's share is always `100 - xaiSharePercent`. 50 = 50/50, 60 = 60/40 (Xai/Nat),
     * 100 = Xai covers all of it, 0 = Nat covers all of it.
     */
    val xaiSharePercent: Int = 50,
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    val natSharePercent: Int get() = 100 - xaiSharePercent

    /** Portion of [amount] that is Xai's responsibility. */
    val xaiShareAmount: Double get() = amount * xaiSharePercent / 100.0

    /** Portion of [amount] that is Nat's responsibility. */
    val natShareAmount: Double get() = amount - xaiShareAmount

    /**
     * Signed contribution of this expense to "how much Xai owes Nat overall".
     * Positive means Xai owes Nat that much for this expense; negative means Nat owes Xai.
     */
    val xaiOwesNat: Double
        get() = if (paidBy == User.NAT) xaiShareAmount else -natShareAmount
}

/** Common split presets shown as quick-pick chips in the add/edit screen. */
enum class SplitPreset(val label: String, val xaiSharePercent: Int) {
    FIFTY_FIFTY("50 / 50", 50),
    SIXTY_FORTY("60 / 40", 60),
    FORTY_SIXTY("40 / 60", 40),
    XAI_ALL("Xai pays all", 100),
    NAT_ALL("Nat pays all", 0)
}
