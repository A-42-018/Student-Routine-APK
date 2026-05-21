package com.alif.studentroutine.data

import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun exportToJson(repository: RoutineRepository): String {
        val root = JSONObject()
        val classes = JSONArray()
        val tasks = JSONArray()
        val notes = JSONArray()

        // ── Classes ───────────────────────────────────────────
        repository.getAllClassesList().forEach { c ->
            classes.put(
                JSONObject()
                    .put("subject", c.subject)
                    .put("dayOfWeek", c.dayOfWeek)
                    .put("startTimeHour", c.startTimeHour)
                    .put("startTimeMinute", c.startTimeMinute)
                    .put("endTimeHour", c.endTimeHour)
                    .put("endTimeMinute", c.endTimeMinute)
                    .put("room", c.room)
                    .put("reminderMinutes", c.reminderMinutes)
            )
        }

        // ── Tasks ─────────────────────────────────────────────
        repository.getAllTasksList().forEach { t ->
            tasks.put(
                JSONObject()
                    .put("title", t.title)
                    .put("description", t.description)
                    .put("dueDate", t.dueDate)
                    .put("priority", t.priority)
                    .put("isCompleted", t.isCompleted)
                    .put("reminderMinutes", t.reminderMinutes)
            )
        }

        // ── Notes (text + checklist only; file/image paths are device-local) ──
        val allClasses = repository.getAllClassesList()
        val classIdToSubject = allClasses.associate { it.id to it.subject }

        val allNotes = repository.getAllNotes().first()

        allNotes.forEach { note ->
            notes.put(
                JSONObject()
                    .put("classSubject", classIdToSubject[note.classId] ?: "")
                    .put("title", note.title)
                    .put("textContent", note.textContent)
                    .put("checklistJson", note.checklistJson)
                    .put("createdAt", note.createdAt)
                    .put("updatedAt", note.updatedAt)
                // imagePaths and filePaths intentionally excluded (device-local)
            )
        }

        return root
            .put("version", 2)
            .put("classes", classes)
            .put("tasks", tasks)
            .put("notes", notes)
            .toString(2)
    }

    suspend fun importFromJson(repository: RoutineRepository, json: String) {
        val root = JSONObject(json)
        val classes = root.optJSONArray("classes") ?: JSONArray()
        val tasks = root.optJSONArray("tasks") ?: JSONArray()
        val notes = root.optJSONArray("notes") ?: JSONArray()

        // ── Clear existing data ───────────────────────────────
        repository.deleteAllTasks()
        repository.deleteAllClasses()
        // Notes are deleted automatically via CASCADE when classes are deleted

        // ── Restore classes, collect new IDs ──────────────────
        // Map subject name → new inserted ID (for re-linking notes)
        val subjectToNewId = mutableMapOf<String, Int>()

        for (i in 0 until classes.length()) {
            val item = classes.getJSONObject(i)
            val newId = repository.insertClass(
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
            subjectToNewId[item.getString("subject")] = newId.toInt()
        }

        // ── Restore tasks ─────────────────────────────────────
        for (i in 0 until tasks.length()) {
            val item = tasks.getJSONObject(i)
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

        // ── Restore notes (re-link via subject name) ──────────
        for (i in 0 until notes.length()) {
            val item = notes.getJSONObject(i)
            val subject = item.optString("classSubject", "")
            val classId = subjectToNewId[subject] ?: continue // skip if class not found

            repository.insertNote(
                ClassNote(
                    classId = classId,
                    title = item.optString("title", ""),
                    textContent = item.optString("textContent", ""),
                    checklistJson = item.optString("checklistJson", "[]"),
                    imagePaths = "[]",   // device-local, cannot restore
                    filePaths = "[]",    // device-local, cannot restore
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }
    }
}