package com.alif.studentroutine.data.database

import androidx.room.*
import com.alif.studentroutine.data.entity.ClassNote
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassNoteDao {

    @Query("SELECT * FROM class_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<ClassNote>>

    @Query("SELECT * FROM class_notes WHERE classId = :classId ORDER BY updatedAt DESC")
    fun getNotesByClassId(classId: Int): Flow<List<ClassNote>>

    @Query("SELECT * FROM class_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): ClassNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ClassNote): Long

    @Update
    suspend fun updateNote(note: ClassNote)

    @Delete
    suspend fun deleteNote(note: ClassNote)

    @Query("DELETE FROM class_notes WHERE classId = :classId")
    suspend fun deleteNotesByClassId(classId: Int)
}