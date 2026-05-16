package com.alif.studentroutine.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val viewModel: AddEditViewModel =
        viewModel(factory = AddEditViewModel.Factory(repository, dataStoreManager))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(2) }
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

    fun fmt(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm)
    }

    val startTimePicker = TimePickerDialog(
        context, { _, h, m -> startHour = h; startMinute = m },
        startHour, startMinute, false
    )
    val endTimePicker = TimePickerDialog(
        context, { _, h, m -> endHour = h; endMinute = m },
        endHour, endMinute, false
    )

    // Sun=1..Sat=7 (Calendar convention), displayed Mon–Sun friendly labels
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val reminderOptions = listOf(0, 5, 10, 15, 30, 60)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (classId == -1) "Add Class" else "Edit Class",
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

                // ── Class Details Card ────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.Book, label = "Class Details")

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            placeholder = { Text("Subject Name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Book,
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
                            value = room,
                            onValueChange = { room = it },
                            placeholder = { Text("Room / Location (Optional)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
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
                            singleLine = true
                        )
                    }
                }

                // ── Day of Week Card ──────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.CalendarToday, label = "Day of Week")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dayLabels.forEachIndexed { index, day ->
                                val dayIndex = index + 1 // Calendar: Sun=1..Sat=7
                                val isSelected = selectedDay == dayIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
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
                                        .clickable { selectedDay = dayIndex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
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

                // ── Time Card ─────────────────────────────────────────
                FormSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormSectionLabel(icon = Icons.Default.AccessTime, label = "Class Time")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TimePickerButton(
                                modifier = Modifier.weight(1f),
                                label = "Start",
                                time = fmt(startHour, startMinute),
                                onClick = { startTimePicker.show() }
                            )
                            // visual separator
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(56.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            )
                            TimePickerButton(
                                modifier = Modifier.weight(1f),
                                label = "End",
                                time = fmt(endHour, endMinute),
                                onClick = { endTimePicker.show() }
                            )
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
                        if (classId == -1) "Add Class" else "Save Changes",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Reusable section card ─────────────────────────────────────────────────────
@Composable
fun FormSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

// ── Section label with icon ───────────────────────────────────────────────────
@Composable
fun FormSectionLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            letterSpacing = 0.3.sp
        )
    }
}

// ── Time picker button ────────────────────────────────────────────────────────
@Composable
fun TimePickerButton(
    modifier: Modifier = Modifier,
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            letterSpacing = 0.3.sp
        )
        Text(
            time,
            fontSize = 16.sp,
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