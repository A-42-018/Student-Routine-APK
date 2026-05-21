package com.alif.studentroutine.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.components.DashboardHeroHeader
import com.alif.studentroutine.ui.components.OffDayCard
import com.alif.studentroutine.ui.theme.HighPriority
import com.alif.studentroutine.ui.theme.LowPriority
import com.alif.studentroutine.ui.theme.MediumPriority
import com.alif.studentroutine.ui.theme.Success
import com.alif.studentroutine.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: RoutineRepository,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToClassNotes: (Int) -> Unit,
    onNavigateToNearbyLibraries: () -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
    val todayClasses by viewModel.todayClasses.collectAsStateWithLifecycle()
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
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
        // Allow Scaffold content to draw behind system bars so the header
        // gradient can flow seamlessly into the status bar area.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTask,
                modifier = Modifier.padding(bottom = 60.dp),
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
                shape = RoundedCornerShape(25.dp)
            )
        }
    ) { scaffoldPadding ->
        // ── Root box: LazyColumn sits behind the header ───────
        // The LazyColumn has NO horizontal padding at the top level.
        // Each section adds its own padding so the header can be truly
        // full-width without any offset/requiredWidth hacks.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                // Only apply bottom scaffold padding; top insets are handled
                // inside DashboardHeroHeader via windowInsetsPadding(statusBars).
                .padding(bottom = scaffoldPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Hero header — full-width, no horizontal padding ────
            // DashboardHeroHeader must call
            //   .windowInsetsPadding(WindowInsets.statusBars)
            // internally so the gradient flows behind the status bar
            // while text/content stays below the system icons.
            item {
                DashboardHeroHeader(
                    greeting = greeting,
                    dateText = "$todayName, $todayDate",
                    todayClassesCount = todayClasses.size,
                    pendingTasksCount = pendingTasks.size,
                    overdueCount = overdueCount,
                    onNearbyLibrariesClick = onNavigateToNearbyLibraries,
                    modifier = Modifier.fillMaxWidth()   // simple fillMaxWidth — no offset needed
                )
            }

            // ── Spacer that accounts for the overlapping stat cards ─
            item {
                Spacer(modifier = Modifier.height(54.dp))
            }

            // ── All sections below get horizontal padding ──────────
            // ── Next Class Highlight ───────────────────────────────
            if (nextClass != null) {
                item {
                    DashNextClassCard(
                        classItem = nextClass,
                        nowMinutes = nowMinutes,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // ── Day Swipe Classes ──────────────────────────────────
            item {
                DashSectionHeader(
                    title = "Classes by Day",
                    onViewAll = onNavigateToTimetable,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item {
                // The pager itself spans full width; its internal cards
                // use their own horizontal padding.
                DayClassesPager(
                    allClasses = allClasses,
                    todayDayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1,
                    nowMinutes = nowMinutes,
                    onNavigateToClassNotes = onNavigateToClassNotes
                )
            }
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
        border = BorderStroke(
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
fun DashNextClassCard(
    classItem: ClassItem,
    nowMinutes: Int,
    modifier: Modifier = Modifier
) {
    val diff = (classItem.startTimeHour * 60 + classItem.startTimeMinute) - nowMinutes
    // diff > 0 is guaranteed by the caller's filter, so "Starting now" is a
    // safety fallback only.
    val countdownText = when {
        diff <= 0 -> "Starting now"
        diff < 60 -> "in $diff min"
        else      -> "in ${diff / 60}h ${diff % 60}m"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
fun DashSectionHeader(
    title: String,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
// nowMinutes = null means "not today" — skips ongoing/past logic entirely
// instead of the magic -1 sentinel.
@Composable
fun DashClassCard(
    classItem: ClassItem,
    nowMinutes: Int?,
    onNotesClick: (() -> Unit)? = null
) {
    val classStart = classItem.startTimeHour * 60 + classItem.startTimeMinute
    val classEnd   = classItem.endTimeHour   * 60 + classItem.endTimeMinute
    val isOngoing  = nowMinutes != null && nowMinutes in classStart until classEnd
    val isPast     = nowMinutes != null && nowMinutes >= classEnd

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOngoing)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
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
                if (onNotesClick != null) {
                    IconButton(
                        onClick = onNotesClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.StickyNote2,
                            contentDescription = "Class notes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
// dateFormat is remembered so it isn't recreated on every recomposition.
@Composable
fun DashTaskCard(task: TaskItem, onComplete: () -> Unit) {
    val isOverdue     = task.dueDate < System.currentTimeMillis()
    val dateFormat    = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val priorityColor = when (task.priority) {
        2    -> HighPriority
        1    -> MediumPriority
        else -> LowPriority
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
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

// ── Day Classes Pager ────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DayClassesPager(
    allClasses: List<ClassItem>,
    todayDayIndex: Int,
    nowMinutes: Int,
    onNavigateToClassNotes: (Int) -> Unit
) {
    val dayLabels     = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayLabelsFull = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val scope         = rememberCoroutineScope()
    val pagerState    = rememberPagerState(initialPage = todayDayIndex) { 7 }

    // Pre-compute classes per day so filtering doesn't run inside the pager
    // frame loop on every swipe frame.
    val classesByDay = remember(allClasses) {
        (0..6).associateWith { dayIndex ->
            allClasses
                .filter { it.dayOfWeek == dayIndex + 1 }
                .sortedWith(compareBy({ it.startTimeHour }, { it.startTimeMinute }))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Day tabs
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            dayLabels.forEachIndexed { index, day ->
                val isSelected = pagerState.currentPage == index
                Tab(
                    selected = isSelected,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            day,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            val pageOffset = ((pagerState.currentPage - page) +
                    pagerState.currentPageOffsetFraction)
                .let { kotlin.math.abs(it) }
                .coerceIn(0f, 1f)
            val scale = 0.85f + (1f - pageOffset) * 0.15f
            val alpha = 0.5f  + (1f - pageOffset) * 0.5f

            val dayClasses = classesByDay[page] ?: emptyList()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                    .padding(horizontal = 16.dp)   // consistent horizontal padding inside pager
            ) {
                if (dayClasses.isEmpty()) {
                    OffDayCard(
                        dayLabel = dayLabelsFull[page],
                        isToday  = page == todayDayIndex
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dayClasses.take(4).forEach { classItem ->
                            DashClassCard(
                                classItem  = classItem,
                                // Pass null for non-today pages — cleaner than magic -1
                                nowMinutes = if (page == todayDayIndex) nowMinutes else null,
                                onNotesClick = { onNavigateToClassNotes(classItem.id) }
                            )
                        }
                        if (dayClasses.size > 4) {
                            Text(
                                "+${dayClasses.size - 4} more",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
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