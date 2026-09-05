package com.xaiworld.expensetracker.data

/** The only two people who ever use this app. */
enum class User(val displayName: String) {
    XAI("Xai"),
    NAT("Nat");

    fun other(): User = if (this == XAI) NAT else XAI
}
