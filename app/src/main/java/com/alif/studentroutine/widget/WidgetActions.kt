package com.alif.studentroutine.widget

import android.content.Context
import android.content.Intent
import com.alif.studentroutine.MainActivity
import com.alif.studentroutine.ui.navigation.Screen

object WidgetActions {
    const val EXTRA_START_ROUTE = "start_route"

    fun mainIntent(context: Context, route: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_START_ROUTE, route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    fun dashboardIntent(context: Context) = mainIntent(context, Screen.Dashboard.route)

    fun tasksIntent(context: Context) = mainIntent(context, Screen.Tasks.route)

    fun classNotesIntent(context: Context, classId: Int) =
        mainIntent(context, Screen.Notes.createRoute(classId))
}
