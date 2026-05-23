package com.alif.studentroutine.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WidgetRefreshScheduler {

    private const val WORK_NAME = "routine_widget_daily_refresh"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
