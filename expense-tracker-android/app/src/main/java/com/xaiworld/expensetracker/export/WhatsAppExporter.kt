package com.xaiworld.expensetracker.export

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.xaiworld.expensetracker.data.Balance
import com.xaiworld.expensetracker.data.Expense
import com.xaiworld.expensetracker.data.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppExporter {

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    fun buildSummary(expenses: List<Expense>, balance: Balance): String {
        val sb = StringBuilder()
        sb.append("*Xai & Nat — Expense summary*\n")
        sb.append("————————————————\n")

        if (expenses.isEmpty()) {
            sb.append("No expenses recorded yet.\n")
        } else {
            for (expense in expenses.sortedByDescending { it.date }) {
                sb.append(dateFormat.format(Date(expense.date)))
                sb.append(" · ")
                sb.append(formatAmount(expense.amount))
                sb.append(" · paid by ").append(expense.paidBy.displayName)
                sb.append(" · split ").append(expense.xaiSharePercent).append("/").append(expense.natSharePercent)
                if (expense.note.isNotBlank()) {
                    sb.append("\n   ").append(expense.note)
                }
                sb.append("\n")
            }
        }

        sb.append("————————————————\n")
        val total = expenses.sumOf { it.amount }
        sb.append("Total spent: ").append(formatAmount(total)).append("\n")
        sb.append(settlementLine(balance))
        return sb.toString()
    }

    private fun settlementLine(balance: Balance): String = when (val owedBy = balance.owedBy) {
        null -> "All settled up! 🎉"
        User.XAI -> "Xai owes Nat ${formatAmount(balance.amount)}"
        User.NAT -> "Nat owes Xai ${formatAmount(balance.amount)}"
    }

    private fun formatAmount(amount: Double): String = String.format(Locale.getDefault(), "%.2f", amount)

    /**
     * Opens WhatsApp with the summary pre-filled, ready to send. Falls back to the normal
     * "share to..." chooser if WhatsApp isn't installed, so the message is never lost.
     */
    fun shareToWhatsApp(context: Context, expenses: List<Expense>, balance: Balance) {
        val text = buildSummary(expenses, balance)
        val whatsAppIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsAppIntent)
        } catch (e: ActivityNotFoundException) {
            val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(chooserIntent, "Share expenses via"))
        }
    }
}
