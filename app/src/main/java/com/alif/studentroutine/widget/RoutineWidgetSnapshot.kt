package com.alif.studentroutine.widget

import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem

data class RoutineWidgetSnapshot(
    val nextClass: ClassItem?,
    val hasClassesToday: Boolean,
    val urgentTasks: List<TaskItem>,
    val overdueCount: Int,
    val greeting: String,
    val dateLabel: String
)
