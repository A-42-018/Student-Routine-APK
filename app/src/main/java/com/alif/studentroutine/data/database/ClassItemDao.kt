package com.alif.studentroutine.data.database

import androidx.room.*
import com.alif.studentroutine.data.entity.ClassItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassItemDao {
    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTimeHour, startTimeMinute")
    fun getAllClasses(): Flow<List<ClassItem>>

    @Query("SELECT * FROM classes ORDER BY dayOfWeek, startTimeHour, startTimeMinute")
    suspend fun getAllClassesList(): List<ClassItem>

    @Query("SELECT * FROM classes WHERE dayOfWeek = :day ORDER BY startTimeHour, startTimeMinute")
    fun getClassesByDay(day: Int): Flow<List<ClassItem>>

    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: Int): ClassItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classItem: ClassItem): Long

    @Update
    suspend fun updateClass(classItem: ClassItem)

    @Delete
    suspend fun deleteClass(classItem: ClassItem)

    @Query("DELETE FROM classes")
    suspend fun deleteAllClasses()
}
