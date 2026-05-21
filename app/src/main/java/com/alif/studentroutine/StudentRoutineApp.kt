package com.alif.studentroutine

import android.app.Application
import com.alif.studentroutine.data.DataStoreManager
import com.alif.studentroutine.data.database.AppDatabase
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.notification.NotificationHelper

class StudentRoutineApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        RoutineRepository(
            database.classItemDao(),
            database.taskItemDao(),
            database.classNoteDao()
        )
    }
    val dataStoreManager by lazy { DataStoreManager(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}