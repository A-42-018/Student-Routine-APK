package com.alif.studentroutine.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        RoutineGlanceWidget().updateAll(applicationContext)
        return Result.success()
    }
}