package com.alif.studentroutine.ui.screens


import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.components.DashEmptyCard
import com.alif.studentroutine.ui.components.OffDayCard
import com.alif.studentroutine.ui.components.RoundedTopHeaderPanel
import com.alif.studentroutine.viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    repository: RoutineRepository,
    onNavigateBack: () -> Unit,
    onAddClass: () -> Unit,
    onEditClass: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: TimetableViewModel =
        viewModel(factory = TimetableViewModel.Factory(repository))
    val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()

    val dayNames = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday"
    )
    val dayShort = listOf("All", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    var selectedDay by remember { mutableIntStateOf(0) }

    val filteredClasses = if (selectedDay == 0) allClasses
    else allClasses.filter { it.dayOfWeek == selectedDay }

    // Stats
    val totalClasses = allClasses.size
    val todayIndex   = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    val todayCount   = allClasses.count { it.dayOfWeek == todayIndex }
    val daysWithClass = allClasses.map { it.dayOfWeek }.distinct().size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RoundedTopHeaderPanel(
                title = "Timetable",
                navigationIcon = Icons.Default.ArrowBack,
                navigationContentDescription = "Back",
                onNavigateBack = onNavigateBack,
                actionIcon = Icons.Default.Add,
                actionContentDescription = "Add Class",
                onActionClick = onAddClass
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {

            // ── Day selector ──────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedDay,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedDay < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedDay])
                                .height(3.dp)
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                },
                divider = {
                    Divider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                }
            ) {
                dayShort.forEachIndexed { index, day ->
                    val classCount = if (index == 0) allClasses.size
                    else allClasses.count { it.dayOfWeek == index }

                    Tab(
                        selected = selectedDay == index,
                        onClick = { selectedDay = index },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = day,
                                fontSize = 13.sp,
                                fontWeight = if (selectedDay == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (classCount > 0) {
                                Spacer(Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(
                                            if (selectedDay == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {

                // ── Stats Row ─────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TimetableStatChip(
                            modifier = Modifier.weight(1f),
                            value = totalClasses.toString(),
                            label = "Total",
                            color = MaterialTheme.colorScheme.primary
                        )
                        TimetableStatChip(
                            modifier = Modifier.weight(1f),
                            value = todayCount.toString(),
                            label = "Today",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TimetableStatChip(
                            modifier = Modifier.weight(1f),
                            value = daysWithClass.toString(),
                            label = "Days/Week",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // ── Section header ────────────────────────────
                item {
                    val headerLabel = if (selectedDay == 0) "All Classes"
                    else "${dayNames[selectedDay - 1]} Classes"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            headerLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${filteredClasses.size} ${if (filteredClasses.size == 1) "class" else "classes"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                        )
                    }
                }

                // ── Class list ────────────────────────────────
                if (filteredClasses.isEmpty()) {
                    item {
                        if (selectedDay == 0) {
                            DashEmptyCard("No classes yet. Add your first class!")
                        } else {
                            OffDayCard(
                                dayLabel = dayNames[selectedDay - 1],
                                isToday = selectedDay == todayIndex
                            )
                        }
                    }
                } else {
                    items(filteredClasses, key = { it.id }) { classItem ->
                        TimetableClassCard(
                            classItem = classItem,
                            showDay = selectedDay == 0,
                            onEdit = { onEditClass(classItem.id) },
                            onDelete = { viewModel.deleteClass(context, classItem) }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun TimetableStatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ── Class Card ────────────────────────────────────────────────────────────────
@Composable
fun TimetableClassCard(
    classItem: ClassItem,
    showDay: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dayNames = listOf(
        "", "Sunday", "Monday", "Tuesday",
        "Wednesday", "Thursday", "Friday", "Saturday"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
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
            // ── Icon box ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // ── Content ───────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = classItem.subject,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (classItem.room.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = classItem.room,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${formatTime(classItem.startTimeHour, classItem.startTimeMinute)}" +
                                    " – ${formatTime(classItem.endTimeHour, classItem.endTimeMinute)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (showDay && classItem.dayOfWeek in 1..7) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = dayNames[classItem.dayOfWeek],
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // ── Actions ───────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Delete Class", fontWeight = FontWeight.Medium)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${classItem.subject}\"?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}