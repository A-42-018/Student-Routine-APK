package com.alif.studentroutine.ui.screens

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
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.viewmodel.AddEditViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassScreen(
    repository: RoutineRepository,
    dataStoreManager: DataStoreManager,
    classId: Int = -1,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddEditViewModel = viewModel(factory = AddEditViewModel.Factory(repository, dataStoreManager))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(2) } // Monday default
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }
    var reminderMinutes by remember { mutableIntStateOf(15) }
    var isLoading by remember { mutableStateOf(classId != -1) }

    LaunchedEffect(classId) {
        if (classId != -1) {
            val existing = viewModel.getClassById(classId)
            existing?.let {
                subject = it.subject
                room = it.room
                selectedDay = it.dayOfWeek
                startHour = it.startTimeHour
                startMinute = it.startTimeMinute
                endHour = it.endTimeHour
                endMinute = it.endTimeMinute
                reminderMinutes = it.reminderMinutes
            }
            isLoading = false
        }
    }

    // ── Helper: format hour & minute to "09:00 AM" style ──
    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
    }

    // ── TimePickerDialog launchers ──
    val startTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            startHour = hour
            startMinute = minute
        },
        startHour,
        startMinute,
        false // false = 12-hour format with AM/PM
    )

    val endTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            endHour = hour
            endMinute = minute
        },
        endHour,
        endMinute,
        false
    )

    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val reminderOptions = listOf(0, 5, 10, 15, 30, 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (classId == -1) "Add Class" else "Edit Class") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (classId != -1) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val existing = viewModel.getClassById(classId)
                                    existing?.let {
                                        viewModel.deleteClass(context, it)
                                        Toast.makeText(context, "Class deleted", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
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

                // ── Subject ──
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name") },
                    leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Room ──
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Location (Optional)") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Day of Week ──
                Text("Day of Week", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayNames.forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDay == index + 1,
                            onClick = { selectedDay = index + 1 },
                            label = { Text(day.take(3)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ── Start Time ──
                Text("Start Time", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { startTimePicker.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(startHour, startMinute),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // ── End Time ──
                Text("End Time", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = { endTimePicker.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(endHour, endMinute),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // ── Reminder ──
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

                // ── Save Button ──
                Button(
                    onClick = {
                        if (subject.isBlank()) {
                            Toast.makeText(context, "Please enter a subject name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                            Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val classItem = ClassItem(
                            id = if (classId == -1) 0 else classId,
                            subject = subject.trim(),
                            dayOfWeek = selectedDay,
                            startTimeHour = startHour,
                            startTimeMinute = startMinute,
                            endTimeHour = endHour,
                            endTimeMinute = endMinute,
                            room = room.trim(),
                            reminderMinutes = reminderMinutes
                        )

                        if (classId == -1) {
                            viewModel.saveClass(context, classItem)
                            Toast.makeText(context, "Class added!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateClass(context, classItem)
                            Toast.makeText(context, "Class updated!", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (classId == -1) "Add Class" else "Save Changes")
                }
            }
        }
    }
}