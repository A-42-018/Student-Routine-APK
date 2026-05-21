package com.alif.studentroutine.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alif.studentroutine.data.database.AppDatabase
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra("type") ?: return
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val id = intent.getIntExtra("id", 0)

        NotificationHelper.showNotification(context, title, message, id)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule alarms on boot
            val database = AppDatabase.getDatabase(context)
            val repository = RoutineRepository(
                database.classItemDao(),
                database.taskItemDao(),
                database.classNoteDao()
            )
            CoroutineScope(Dispatchers.IO).launch {
                AlarmScheduler.rescheduleAll(context, repository)
            }
        }
    }
}

object AlarmScheduler {

    fun scheduleClassReminder(context: Context, classItem: ClassItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "class")
            putExtra("title", "Upcoming Class: ${classItem.subject}")
            putExtra("message", "Starts at ${formatTime(classItem.startTimeHour, classItem.startTimeMinute)}${if (classItem.room.isNotEmpty()) " in ${classItem.room}" else ""}")
            putExtra("id", classItem.id + 10000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            classItem.id + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, classItem.dayOfWeek)
            set(Calendar.HOUR_OF_DAY, classItem.startTimeHour)
            set(Calendar.MINUTE, classItem.startTimeMinute)
            set(Calendar.SECOND, 0)
            add(Calendar.MINUTE, -classItem.reminderMinutes)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleTaskReminder(context: Context, taskItem: TaskItem) {
        if (taskItem.isCompleted) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("type", "task")
            putExtra("title", "Task Due: ${taskItem.title}")
            putExtra("message", taskItem.description.ifEmpty { "Don't forget your task!" })
            putExtra("id", taskItem.id + 20000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskItem.id + 20000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = taskItem.dueDate - (taskItem.reminderMinutes * 60 * 1000)
        if (triggerTime <= System.currentTimeMillis()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, id: Int, offset: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id + offset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    suspend fun cancelAll(context: Context, repository: RoutineRepository) {
        repository.getAllClassesList().forEach { classItem ->
            cancelReminder(context, classItem.id, 10000)
        }
        repository.getPendingTasksList().forEach { taskItem ->
            cancelReminder(context, taskItem.id, 20000)
        }
    }

    suspend fun rescheduleAll(context: Context, repository: RoutineRepository) {
        repository.getAllClassesList().forEach { classItem ->
            scheduleClassReminder(context, classItem)
        }
        repository.getPendingTasksList().forEach { taskItem ->
            scheduleTaskReminder(context, taskItem)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        val m = minute.toString().padStart(2, '0')
        return "$h:$m $amPm"
    }
}
