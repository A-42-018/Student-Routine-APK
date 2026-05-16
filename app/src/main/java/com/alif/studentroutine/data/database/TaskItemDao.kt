package com.alif.studentroutine.data.database

import androidx.room.*
import com.alif.studentroutine.data.entity.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskItemDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    suspend fun getAllTasksList(): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
    suspend fun getPendingTasksList(): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(taskItem: TaskItem): Long

    @Update
    suspend fun updateTask(taskItem: TaskItem)

    @Delete
    suspend fun deleteTask(taskItem: TaskItem)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
