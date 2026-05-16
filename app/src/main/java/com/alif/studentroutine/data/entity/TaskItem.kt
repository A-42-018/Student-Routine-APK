package com.alif.studentroutine.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long, // timestamp
    val priority: Int = 1, // 0 = Low, 1 = Medium, 2 = High
    val isCompleted: Boolean = false,
    val reminderMinutes: Int = 30
)
