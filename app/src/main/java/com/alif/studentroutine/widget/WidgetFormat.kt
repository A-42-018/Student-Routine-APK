package com.alif.studentroutine.widget

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatClassTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm)
}

internal fun formatTaskDue(dueDate: Long): String {
    val fmt = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return fmt.format(Date(dueDate))
}

internal fun priorityLabel(priority: Int): String = when (priority) {
    2 -> "High"
    0 -> "Low"
    else -> "Medium"
}
