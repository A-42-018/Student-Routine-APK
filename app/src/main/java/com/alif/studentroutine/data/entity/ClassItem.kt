package com.alif.studentroutine.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val dayOfWeek: Int, // 1 = Sunday, 7 = Saturday
    val startTimeHour: Int,
    val startTimeMinute: Int,
    val endTimeHour: Int,
    val endTimeMinute: Int,
    val room: String = "",
    val reminderMinutes: Int = 15
)
