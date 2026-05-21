package com.alif.studentroutine.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.entity.TaskItem

@Database(
    entities = [ClassItem::class, TaskItem::class, ClassNote::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classItemDao(): ClassItemDao
    abstract fun taskItemDao(): TaskItemDao
    abstract fun classNoteDao(): ClassNoteDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `class_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `classId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL DEFAULT '',
                        `textContent` TEXT NOT NULL DEFAULT '',
                        `checklistJson` TEXT NOT NULL DEFAULT '[]',
                        `imagePaths` TEXT NOT NULL DEFAULT '[]',
                        `filePaths` TEXT NOT NULL DEFAULT '[]',
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`classId`) REFERENCES `classes`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_class_notes_classId` ON `class_notes` (`classId`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_routine_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}