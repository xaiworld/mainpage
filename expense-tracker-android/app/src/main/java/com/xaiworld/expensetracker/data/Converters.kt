package com.xaiworld.expensetracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromUser(user: User): String = user.name

    @TypeConverter
    fun toUser(value: String): User = User.valueOf(value)
}
