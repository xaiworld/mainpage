package com.xaiworld.expensetracker.data

import android.content.Context

/**
 * Tiny wrapper around SharedPreferences for the one setting each phone needs:
 * "which of the two of us is using this phone". That answer is used both as the default
 * "paid by" value when logging a new expense, and as the name this phone advertises itself
 * with during Nearby sync so the other phone shows a friendly name instead of a random id.
 */
class Prefs(context: Context) {
    private val sharedPrefs = context.applicationContext
        .getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    var localUser: User?
        get() = sharedPrefs.getString(KEY_LOCAL_USER, null)?.let { runCatching { User.valueOf(it) }.getOrNull() }
        set(value) = sharedPrefs.edit().putString(KEY_LOCAL_USER, value?.name).apply()

    companion object {
        private const val KEY_LOCAL_USER = "local_user"
    }
}
