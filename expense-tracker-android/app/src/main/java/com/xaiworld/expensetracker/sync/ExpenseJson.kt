package com.xaiworld.expensetracker.sync

import com.xaiworld.expensetracker.data.Expense
import com.xaiworld.expensetracker.data.User
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manual, dependency-free JSON (de)serialization for the payload the two phones exchange
 * over Nearby Connections. Kept intentionally simple (org.json ships with Android) so the
 * wire format never depends on a serialization library version.
 */
object ExpenseJson {

    fun encodeList(expenses: List<Expense>): String {
        val array = JSONArray()
        for (expense in expenses) {
            val obj = JSONObject()
            obj.put("id", expense.id)
            obj.put("amount", expense.amount)
            obj.put("date", expense.date)
            obj.put("paidBy", expense.paidBy.name)
            obj.put("note", expense.note)
            obj.put("xaiSharePercent", expense.xaiSharePercent)
            obj.put("lastModified", expense.lastModified)
            obj.put("isDeleted", expense.isDeleted)
            array.put(obj)
        }
        return array.toString()
    }

    fun decodeList(json: String): List<Expense> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            Expense(
                id = obj.getString("id"),
                amount = obj.getDouble("amount"),
                date = obj.getLong("date"),
                paidBy = User.valueOf(obj.getString("paidBy")),
                note = obj.optString("note", ""),
                xaiSharePercent = obj.optInt("xaiSharePercent", 50),
                lastModified = obj.getLong("lastModified"),
                isDeleted = obj.optBoolean("isDeleted", false)
            )
        }
    }
}
