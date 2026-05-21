package com.alif.studentroutine.data.repository

import com.alif.studentroutine.data.database.ClassItemDao
import com.alif.studentroutine.data.database.ClassNoteDao
import com.alif.studentroutine.data.database.TaskItemDao
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.entity.TaskItem
import kotlinx.coroutines.flow.Flow

class RoutineRepository(
    private val classItemDao: ClassItemDao,
    private val taskItemDao: TaskItemDao,
    private val classNoteDao: ClassNoteDao
) {
    // Classes
    fun getAllClasses(): Flow<List<ClassItem>> = classItemDao.getAllClasses()
    suspend fun getAllClassesList(): List<ClassItem> = classItemDao.getAllClassesList()
    fun getClassesByDay(day: Int): Flow<List<ClassItem>> = classItemDao.getClassesByDay(day)
    suspend fun getClassById(id: Int): ClassItem? = classItemDao.getClassById(id)
    suspend fun insertClass(classItem: ClassItem): Long = classItemDao.insertClass(classItem)
    suspend fun updateClass(classItem: ClassItem) = classItemDao.updateClass(classItem)
    suspend fun deleteClass(classItem: ClassItem) = classItemDao.deleteClass(classItem)
    suspend fun deleteAllClasses() = classItemDao.deleteAllClasses()

    // Tasks
    fun getAllTasks(): Flow<List<TaskItem>> = taskItemDao.getAllTasks()
    suspend fun getAllTasksList(): List<TaskItem> = taskItemDao.getAllTasksList()
    fun getPendingTasks(): Flow<List<TaskItem>> = taskItemDao.getPendingTasks()
    suspend fun getPendingTasksList(): List<TaskItem> = taskItemDao.getPendingTasksList()
    suspend fun getTaskById(id: Int): TaskItem? = taskItemDao.getTaskById(id)
    suspend fun insertTask(taskItem: TaskItem): Long = taskItemDao.insertTask(taskItem)
    suspend fun updateTask(taskItem: TaskItem) = taskItemDao.updateTask(taskItem)
    suspend fun deleteTask(taskItem: TaskItem) = taskItemDao.deleteTask(taskItem)
    suspend fun deleteAllTasks() = taskItemDao.deleteAllTasks()

    // Notes
    fun getAllNotes(): Flow<List<ClassNote>> = classNoteDao.getAllNotes()
    fun getNotesByClassId(classId: Int): Flow<List<ClassNote>> = classNoteDao.getNotesByClassId(classId)
    suspend fun getNoteById(id: Int): ClassNote? = classNoteDao.getNoteById(id)
    suspend fun insertNote(note: ClassNote): Long = classNoteDao.insertNote(note)
    suspend fun updateNote(note: ClassNote) = classNoteDao.updateNote(note)
    suspend fun deleteNote(note: ClassNote) = classNoteDao.deleteNote(note)
    suspend fun deleteNotesByClassId(classId: Int) = classNoteDao.deleteNotesByClassId(classId)
}