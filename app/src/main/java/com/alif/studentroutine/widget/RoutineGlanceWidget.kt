package com.alif.studentroutine.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.alif.studentroutine.StudentRoutineApp
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoutineGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = withContext(Dispatchers.IO) {
            val app = context.applicationContext as StudentRoutineApp
            RoutineSnapshotBuilder.build(app.repository)
        }
        provideContent {
            RoutineWidgetContent(snapshot)
        }
    }
}

@Composable
private fun RoutineWidgetContent(snapshot: RoutineWidgetSnapshot) {
    val context = LocalContext.current
    val palette = WidgetPalette

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(palette.primary)
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(WidgetActions.dashboardIntent(context))),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = snapshot.greeting,
                        style = TextStyle(
                            color = palette.onPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = snapshot.dateLabel,
                        style = TextStyle(color = palette.onPrimaryMuted, fontSize = 11.sp)
                    )
                }
                if (snapshot.overdueCount > 0) {
                    Text(
                        text = "${snapshot.overdueCount} overdue",
                        style = TextStyle(
                            color = palette.error,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            NextClassSection(snapshot = snapshot, context = context)

            Spacer(GlanceModifier.height(8.dp))

            TasksSection(tasks = snapshot.urgentTasks, context = context)

            Spacer(GlanceModifier.height(6.dp))

            Text(
                text = "Student Routine",
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(WidgetActions.dashboardIntent(context))),
                style = TextStyle(
                    color = palette.onPrimaryMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun NextClassSection(snapshot: RoutineWidgetSnapshot, context: Context) {
    val nextClass = snapshot.nextClass
    val palette = WidgetPalette
    val sectionModifier = GlanceModifier
        .fillMaxWidth()
        .background(palette.surface)
        .cornerRadius(12.dp)
        .padding(10.dp)
        .then(
            if (nextClass != null) {
                GlanceModifier.clickable(
                    actionStartActivity(WidgetActions.classNotesIntent(context, nextClass.id))
                )
            } else {
                GlanceModifier.clickable(
                    actionStartActivity(WidgetActions.dashboardIntent(context))
                )
            }
        )

    Column(modifier = sectionModifier) {
        Text(
            text = "Next class",
            style = TextStyle(color = palette.textGray, fontSize = 10.sp)
        )
        Spacer(GlanceModifier.height(4.dp))
        when {
            nextClass != null -> NextClassRow(nextClass)
            snapshot.hasClassesToday -> {
                Text(
                    text = "No more classes today",
                    style = TextStyle(
                        color = palette.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            else -> {
                Text(
                    text = "No classes scheduled today",
                    style = TextStyle(
                        color = palette.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun NextClassRow(classItem: ClassItem) {
    val palette = WidgetPalette
    Text(
        text = classItem.subject,
        style = TextStyle(
            color = palette.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    )
    val timeText = formatClassTime(classItem.startTimeHour, classItem.startTimeMinute)
    val detail = if (classItem.room.isNotEmpty()) {
        "$timeText · ${classItem.room}"
    } else {
        timeText
    }
    Text(
        text = detail,
        style = TextStyle(color = palette.secondary, fontSize = 11.sp)
    )
}

@Composable
private fun TasksSection(tasks: List<TaskItem>, context: Context) {
    val palette = WidgetPalette
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(palette.surface)
            .cornerRadius(12.dp)
            .padding(10.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(WidgetActions.tasksIntent(context))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tasks",
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = palette.textGray, fontSize = 10.sp)
            )
            Text(
                text = "View all",
                style = TextStyle(color = palette.primaryText, fontSize = 10.sp)
            )
        }

        if (tasks.isEmpty()) {
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "No pending tasks",
                style = TextStyle(color = palette.onSurface, fontSize = 12.sp)
            )
        } else {
            tasks.forEach { task ->
                Spacer(GlanceModifier.height(6.dp))
                TaskRow(task, context)
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskItem, context: Context) {
    val palette = WidgetPalette
    val isOverdue = task.dueDate < System.currentTimeMillis()
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionStartActivity(WidgetActions.tasksIntent(context)))
    ) {
        Text(
            text = task.title,
            style = TextStyle(
                color = if (isOverdue) palette.error else palette.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
        Text(
            text = "${priorityLabel(task.priority)} · Due ${formatTaskDue(task.dueDate)}",
            style = TextStyle(color = palette.textGray, fontSize = 10.sp),
            maxLines = 1
        )
    }
}
