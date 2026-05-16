package com.alif.studentroutine.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val viewModel: AddEditViewModel =
        viewModel(factory = AddEditViewModel.Factory(repository, dataStoreManager))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calendar = Calendar.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(calendar.timeInMillis + 86400000) }
    var priority by remember { mutableIntStateOf(1) }
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

    val dateDisplayFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
    val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    val priorityLabels = listOf("Low", "Medium", "High")
    val priorityColors = listOf(LowPriority, MediumPriority, HighPriority)
    val reminderOptions = listOf(0, 5, 10, 15, 30, 60)

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val c = Calendar.getInstance().apply {
                timeInMillis = dueDate
                set(year, month, day)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (taskId == -1) "Add Task" else "Edit Task",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Task Details Card ─────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.CheckCircle, label = "Task Details")

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = {
                                Text(
                                    "Task Title",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Title,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = {
                                Text(
                                    "Description (Optional)",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                            ),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                }

                // ── Due Date & Time Card ──────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.CalendarToday, label = "Due Date & Time")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Date button
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
                                    .clickable { datePicker.show() }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "DATE",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    dateDisplayFormat.format(Date(dueDate)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(56.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            )

                            // Time button
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
                                    .clickable { timePicker.show() }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "TIME",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    timeDisplayFormat.format(Date(dueDate)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // ── Priority Card ─────────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.Flag, label = "Priority")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priorityLabels.forEachIndexed { index, label ->
                                val isSelected = priority == index
                                val chipColor = priorityColors[index]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) chipColor.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.dp else 0.5.dp,
                                            color = if (isSelected) chipColor.copy(alpha = 0.6f)
                                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { priority = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) chipColor
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Reminder Card ─────────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.NotificationsActive, label = "Reminder")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reminderOptions.forEach { mins ->
                                val isSelected = reminderMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                                        )
                                        .border(
                                            width = if (isSelected) 0.dp else 0.5.dp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { reminderMinutes = mins }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (mins == 0) "None" else if (mins == 60) "1h" else "${mins}m",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Save Button ───────────────────────────────────────
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (taskId == -1) "Add Task" else "Save Changes",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}