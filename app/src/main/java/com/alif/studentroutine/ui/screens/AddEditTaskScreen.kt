package com.alif.studentroutine.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.DataStoreManager
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.theme.*
import com.alif.studentroutine.viewmodel.AddEditViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    repository: RoutineRepository,
    dataStoreManager: DataStoreManager,
    taskId: Int = -1,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditViewModel = viewModel(factory = AddEditViewModel.Factory(repository, dataStoreManager))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(calendar.timeInMillis + 86400000) } // tomorrow
    var priority by remember { mutableIntStateOf(1) } // Medium
    var reminderMinutes by remember { mutableIntStateOf(30) }
    var isLoading by remember { mutableStateOf(taskId != -1) }

    LaunchedEffect(taskId) {
        if (taskId != -1) {
            val existing = viewModel.getTaskById(taskId)
            existing?.let {
                title = it.title
                description = it.description
                dueDate = it.dueDate
                priority = it.priority
                reminderMinutes = it.reminderMinutes
            }
            isLoading = false
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val priorityOptions = listOf("Low", "Medium", "High")
    val priorityColors = listOf(LowPriority, MediumPriority, HighPriority)
    val reminderOptions = listOf(0, 5, 10, 15, 30, 60)

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val c = Calendar.getInstance().apply {
                set(year, month, day)
                set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
            }
            dueDate = c.timeInMillis
            calendar.timeInMillis = dueDate
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            dueDate = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1) "Add Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (taskId != -1) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val existing = viewModel.getTaskById(taskId)
                                    existing?.let {
                                        viewModel.deleteTask(context, it)
                                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Text("Due Date & Time", style = MaterialTheme.typography.titleMedium)
                Card(
                    onClick = {
                        datePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = dateFormat.format(Date(dueDate)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Button(
                    onClick = { timePicker.show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Time")
                }

                Text("Priority", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorityOptions.forEachIndexed { index, label ->
                        FilterChip(
                            selected = priority == index,
                            onClick = { priority = index },
                            label = { Text(label) }
                        )
                    }
                }

                Text("Reminder", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reminderOptions.forEach { mins ->
                        FilterChip(
                            selected = reminderMinutes == mins,
                            onClick = { reminderMinutes = mins },
                            label = { Text(if (mins == 0) "None" else "${mins}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a task title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (dueDate <= System.currentTimeMillis()) {
                            Toast.makeText(context, "Due date must be in the future", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val taskItem = TaskItem(
                            id = if (taskId == -1) 0 else taskId,
                            title = title.trim(),
                            description = description.trim(),
                            dueDate = dueDate,
                            priority = priority,
                            reminderMinutes = reminderMinutes
                        )

                        if (taskId == -1) {
                            viewModel.saveTask(context, taskItem)
                            Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateTask(context, taskItem)
                            Toast.makeText(context, "Task updated!", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (taskId == -1) "Add Task" else "Save Changes")
                }
            }
        }
    }
}