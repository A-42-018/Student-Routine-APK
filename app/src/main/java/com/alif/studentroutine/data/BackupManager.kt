package com.alif.studentroutine.data

import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {
    suspend fun exportToJson(repository: RoutineRepository): String {
        val root = JSONObject()
        val classes = JSONArray()
        val tasks = JSONArray()

        repository.getAllClassesList().forEach { classItem ->
            classes.put(
                JSONObject()
                    .put("subject", classItem.subject)
                    .put("dayOfWeek", classItem.dayOfWeek)
                    .put("startTimeHour", classItem.startTimeHour)
                    .put("startTimeMinute", classItem.startTimeMinute)
                    .put("endTimeHour", classItem.endTimeHour)
                    .put("endTimeMinute", classItem.endTimeMinute)
                    .put("room", classItem.room)
                    .put("reminderMinutes", classItem.reminderMinutes)
            )
        }

        repository.getAllTasksList().forEach { taskItem ->
            tasks.put(
                JSONObject()
                    .put("title", taskItem.title)
                    .put("description", taskItem.description)
                    .put("dueDate", taskItem.dueDate)
                    .put("priority", taskItem.priority)
                    .put("isCompleted", taskItem.isCompleted)
                    .put("reminderMinutes", taskItem.reminderMinutes)
            )
        }

        return root
            .put("version", 1)
            .put("classes", classes)
            .put("tasks", tasks)
            .toString(2)
    }

    suspend fun importFromJson(repository: RoutineRepository, json: String) {
        val root = JSONObject(json)
        val classes = root.optJSONArray("classes") ?: JSONArray()
        val tasks = root.optJSONArray("tasks") ?: JSONArray()

        repository.deleteAllTasks()
        repository.deleteAllClasses()

        for (index in 0 until classes.length()) {
            val item = classes.getJSONObject(index)
            repository.insertClass(
                ClassItem(
                    subject = item.getString("subject"),
                    dayOfWeek = item.getInt("dayOfWeek"),
                    startTimeHour = item.getInt("startTimeHour"),
                    startTimeMinute = item.getInt("startTimeMinute"),
                    endTimeHour = item.getInt("endTimeHour"),
                    endTimeMinute = item.getInt("endTimeMinute"),
                    room = item.optString("room", ""),
                    reminderMinutes = item.optInt("reminderMinutes", 30)
                )
            )
        }

        for (index in 0 until tasks.length()) {
            val item = tasks.getJSONObject(index)
            repository.insertTask(
                TaskItem(
                    title = item.getString("title"),
                    description = item.optString("description", ""),
                    dueDate = item.getLong("dueDate"),
                    priority = item.optInt("priority", 1),
                    isCompleted = item.optBoolean("isCompleted", false),
                    reminderMinutes = item.optInt("reminderMinutes", 30)
                )
            )
        }
    }
}
