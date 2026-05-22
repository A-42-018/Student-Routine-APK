package com.alif.studentroutine.ui.screens
import com.alif.studentroutine.ui.components.DashEmptyCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.components.RoundedTopHeaderPanel
import com.alif.studentroutine.ui.theme.*
import com.alif.studentroutine.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    repository: RoutineRepository,
    onAddTask: () -> Unit,
    onEditTask: (Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory(repository))
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    var showCompleted by remember { mutableStateOf(true) }
    val displayedTasks = if (showCompleted) allTasks else allTasks.filter { !it.isCompleted }

    val pendingCount  = allTasks.count { !it.isCompleted }
    val overdueCount  = allTasks.count { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
    val completedCount = allTasks.count { it.isCompleted }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RoundedTopHeaderPanel(
                title = "Tasks",
                actionIcon = Icons.Default.Add,
                actionContentDescription = "Add Task",
                onActionClick = onAddTask
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
        ) {

            // ── Stats Row ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TaskStatChip(
                        modifier = Modifier.weight(1f),
                        value = pendingCount.toString(),
                        label = "Pending",
                        color = MaterialTheme.colorScheme.primary
                    )
                    TaskStatChip(
                        modifier = Modifier.weight(1f),
                        value = overdueCount.toString(),
                        label = "Overdue",
                        color = if (overdueCount > 0) MaterialTheme.colorScheme.error else Success
                    )
                    TaskStatChip(
                        modifier = Modifier.weight(1f),
                        value = completedCount.toString(),
                        label = "Done",
                        color = Success
                    )
                }
            }

            // ── Filter toggle ─────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showCompleted) "All Tasks" else "Pending Only",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Toggle pill
                    Surface(
                        onClick = { showCompleted = !showCompleted },
                        shape = RoundedCornerShape(20.dp),
                        color = if (showCompleted)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (showCompleted)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (showCompleted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (showCompleted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (showCompleted) "Show All" else "Pending Only",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (showCompleted)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }

            // ── Task list ─────────────────────────────────────
            if (displayedTasks.isEmpty()) {
                item {
                    DashEmptyCard(
                        if (showCompleted) "No tasks yet. Add your first task!"
                        else "No pending tasks. Great job! ✅"
                    )
                }
            } else {
                items(displayedTasks, key = { it.id }) { task ->
                    FullTaskCard(
                        task = task,
                        onToggleComplete = { viewModel.toggleTaskComplete(context, task) },
                        onEdit = { onEditTask(task.id) },
                        onDelete = { viewModel.deleteTask(context, task) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun TaskStatChip(
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

// ── Full Task Card ────────────────────────────────────────────────────────────
@Composable
fun FullTaskCard(
    task: TaskItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isOverdue     = !task.isCompleted && task.dueDate < System.currentTimeMillis()
    val priorityColor = when (task.priority) {
        2    -> HighPriority
        1    -> MediumPriority
        else -> LowPriority
    }
    val priorityLabels = listOf("Low", "Medium", "High")
    val dateFormat     = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                task.isCompleted -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                isOverdue        -> MaterialTheme.colorScheme.error.copy(alpha = 0.04f)
                else             -> MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOverdue) 1.dp else 0.5.dp,
            color = when {
                isOverdue        -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                task.isCompleted -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)
                else             -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Priority dot + Checkbox ───────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            else priorityColor
                        )
                )
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.size(20.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Success,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                )
            }

            // ── Content ───────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = priorityColor.copy(alpha = if (task.isCompleted) 0.07f else 0.13f)
                    ) {
                        Text(
                            text = priorityLabels[task.priority.coerceIn(0, 2)],
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (task.isCompleted)
                                priorityColor.copy(alpha = 0.45f)
                            else
                                priorityColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "Due: ${dateFormat.format(Date(task.dueDate))}",
                        fontSize = 11.sp,
                        color = when {
                            isOverdue        -> MaterialTheme.colorScheme.error
                            task.isCompleted -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            else             -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                Text("Delete Task", fontWeight = FontWeight.Medium)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${task.title}\"?",
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
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
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