package com.alif.studentroutine.data.repository

import com.alif.studentroutine.data.database.ClassItemDao
import com.alif.studentroutine.data.database.TaskItemDao
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import kotlinx.coroutines.flow.Flow

class RoutineRepository(
    private val classItemDao: ClassItemDao,
    private val taskItemDao: TaskItemDao
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
}
