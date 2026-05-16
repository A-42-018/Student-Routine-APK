package com.alif.studentroutine.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem

@Database(entities = [ClassItem::class, TaskItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classItemDao(): ClassItemDao
    abstract fun taskItemDao(): TaskItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_routine_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
