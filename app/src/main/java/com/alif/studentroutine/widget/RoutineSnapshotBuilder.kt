package com.alif.studentroutine.widget

import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object RoutineSnapshotBuilder {

    suspend fun build(repository: RoutineRepository): RoutineWidgetSnapshot {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val nowMinutes = currentHour * 60 + calendar.get(Calendar.MINUTE)

        val greeting = when {
            currentHour < 12 -> "Good Morning"
            currentHour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        val dateLabel = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            .format(calendar.time)

        val todayClasses = repository.getClassesByDay(today).first()
        val nextClass = todayClasses
            .filter { it.startTimeHour * 60 + it.startTimeMinute > nowMinutes }
            .minByOrNull { it.startTimeHour * 60 + it.startTimeMinute }

        val allPending = repository.getPendingTasksList()
        val now = System.currentTimeMillis()
        val urgentTasks = allPending
            .sortedBy { it.dueDate }
            .take(3)
        val overdueCount = allPending.count { it.dueDate < now }

        return RoutineWidgetSnapshot(
            nextClass = nextClass,
            hasClassesToday = todayClasses.isNotEmpty(),
            urgentTasks = urgentTasks,
            overdueCount = overdueCount,
            greeting = greeting,
            dateLabel = dateLabel
        )
    }
}
