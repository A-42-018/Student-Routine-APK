package com.alif.studentroutine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.theme.HighPriority
import com.alif.studentroutine.ui.theme.LowPriority
import com.alif.studentroutine.ui.theme.MediumPriority
import com.alif.studentroutine.ui.theme.Success
import com.alif.studentroutine.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: RoutineRepository,
    onNavigateToTimetable: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNearbyLibraries: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val pendingTasks by viewModel.pendingTasks.collectAsStateWithLifecycle()

    // ── Date / time info ──────────────────────────────────────
    val calendar    = Calendar.getInstance()
    val dayNames    = arrayOf("", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val todayName   = dayNames[calendar.get(Calendar.DAY_OF_WEEK)]
    val dateFormat  = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val todayDate   = dateFormat.format(calendar.time)
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting    = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else             -> "Good Evening"
    }

    // ── Next upcoming class ───────────────────────────────────
    val nowMinutes = currentHour * 60 + calendar.get(Calendar.MINUTE)
    val nextClass  = todayClasses
        .filter { it.startTimeHour * 60 + it.startTimeMinute > nowMinutes }
        .minByOrNull { it.startTimeHour * 60 + it.startTimeMinute }

    val overdueCount = pendingTasks.count { it.dueDate < System.currentTimeMillis() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Student Routine",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateToAddClass,
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Class",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddTask,
                    icon = {
                        Icon(
                            Icons.Default.AddTask,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    text = {
                        Text(
                            "Add Task",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // ── Greeting ──────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "$greeting, Alif 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$todayName, $todayDate",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }
            }

            // ── Stats Row ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Today's Classes",
                        value = todayClasses.size.toString(),
                        subText = if (todayClasses.isEmpty()) "No classes" else "${todayClasses.size} remaining",
                        subColor = MaterialTheme.colorScheme.primary,
                        progress = if (todayClasses.isEmpty()) 0f else 1f,
                        progressColor = MaterialTheme.colorScheme.primary
                    )
                    DashStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Pending Tasks",
                        value = pendingTasks.size.toString(),
                        subText = if (overdueCount > 0) "$overdueCount overdue" else "All on track",
                        subColor = if (overdueCount > 0) MaterialTheme.colorScheme.error else Success,
                        progress = if (pendingTasks.isEmpty()) 0f
                        else overdueCount.toFloat() / pendingTasks.size,
                        progressColor = if (overdueCount > 0) MaterialTheme.colorScheme.error else Success
                    )
                }
            }

            // ── Next Class Highlight ───────────────────────────
            if (nextClass != null) {
                item {
                    DashNextClassCard(classItem = nextClass, nowMinutes = nowMinutes)
                }
            }

            // ── Today's Classes ───────────────────────────────
            item {
                DashSectionHeader(title = "Today's Classes", onViewAll = onNavigateToTimetable)
            }

            if (todayClasses.isEmpty()) {
                item { DashEmptyCard("No classes today! Enjoy your free time 🎉") }
            } else {
                items(todayClasses) { classItem ->
                    DashClassCard(classItem = classItem, nowMinutes = nowMinutes)
                }
            }

            // ── Pending Tasks ─────────────────────────────────
            item {
                DashSectionHeader(title = "Pending Tasks", onViewAll = onNavigateToTasks)
            }

            if (pendingTasks.isEmpty()) {
                item { DashEmptyCard("All tasks completed! Great job ✅") }
            } else {
                items(pendingTasks.take(5)) { task ->
                    DashTaskCard(
                        task = task,
                        onComplete = { viewModel.markTaskComplete(task) }
                    )
                }
            }

            // ── Find Libraries ────────────────────────────────
            item {
                Card(
                    onClick = onNavigateToNearbyLibraries,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Find Libraries Near You",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "Discover study spots nearby",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────────────────
@Composable
fun DashStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subText: String,
    subColor: Color,
    progress: Float,
    progressColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                letterSpacing = 0.3.sp
            )
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(subText, fontSize = 11.sp, color = subColor)
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(progressColor)
                )
            }
        }
    }
}

// ── Next Class Card ───────────────────────────────────────────────────────────
@Composable
fun DashNextClassCard(classItem: ClassItem, nowMinutes: Int) {
    val diff = (classItem.startTimeHour * 60 + classItem.startTimeMinute) - nowMinutes
    val countdownText = when {
        diff <= 0  -> "Starting now"
        diff < 60  -> "in $diff min"
        else       -> "in ${diff / 60}h ${diff % 60}m"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "NEXT CLASS",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Text(
                classItem.subject,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        formatTime(classItem.startTimeHour, classItem.startTimeMinute) +
                                if (classItem.room.isNotEmpty()) " · Room ${classItem.room}" else "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                ) {
                    Text(
                        countdownText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
fun DashSectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onViewAll, contentPadding = PaddingValues(0.dp)) {
            Text("View All", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── Class Card ────────────────────────────────────────────────────────────────
@Composable
fun DashClassCard(classItem: ClassItem, nowMinutes: Int) {
    val classStart = classItem.startTimeHour * 60 + classItem.startTimeMinute
    val classEnd   = classItem.endTimeHour * 60 + classItem.endTimeMinute
    val isOngoing  = nowMinutes in classStart until classEnd
    val isPast     = nowMinutes >= classEnd

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOngoing)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOngoing) 1.dp else 0.5.dp,
            color = if (isOngoing)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = if (isPast)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    classItem.subject,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPast)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (classItem.room.isNotEmpty()) {
                    Text(
                        "Room ${classItem.room}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    formatTime(classItem.startTimeHour, classItem.startTimeMinute),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPast)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.primary
                )
                when {
                    isOngoing -> Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Success.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Ongoing",
                            fontSize = 10.sp,
                            color = Success,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    isPast -> Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                    ) {
                        Text(
                            "Done",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Task Card ─────────────────────────────────────────────────────────────────
@Composable
fun DashTaskCard(task: TaskItem, onComplete: () -> Unit) {
    val isOverdue     = task.dueDate < System.currentTimeMillis()
    val dateFormat    = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val priorityColor = when (task.priority) {
        2    -> HighPriority
        1    -> MediumPriority
        else -> LowPriority
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isOverdue)
                MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Due: ${dateFormat.format(Date(task.dueDate))}",
                        fontSize = 11.sp,
                        color = if (isOverdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Overdue",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = onComplete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Mark complete",
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────
@Composable
fun DashEmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ── Time formatter ────────────────────────────────────────────────────────────
internal fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val h    = if (hour % 12 == 0) 12 else hour % 12
    val m    = minute.toString().padStart(2, '0')
    return "$h:$m $amPm"
}