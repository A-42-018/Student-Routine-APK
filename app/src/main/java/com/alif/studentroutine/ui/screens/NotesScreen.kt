package com.alif.studentroutine.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.NoteAttachmentHelper
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.ui.components.DashEmptyCard
import com.alif.studentroutine.ui.components.RoundedTopHeaderPanel
import com.alif.studentroutine.viewmodel.NotesViewModel
import com.alif.studentroutine.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(
    repository: RoutineRepository,
    filterClassId: Int? = null,
    onAddNote: () -> Unit,
    onEditNote: (Int) -> Unit
) {
    val viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory(repository))
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))

    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val allClasses by dashboardViewModel.allClasses.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    // Build classId → subject name map
    val classMap = remember(allClasses) { allClasses.associateBy({ it.id }, { it.subject }) }

    val filteredNotes = remember(allNotes, searchQuery, classMap, filterClassId) {
        val byClass = if (filterClassId != null) {
            allNotes.filter { it.classId == filterClassId }
        } else {
            allNotes
        }
        if (searchQuery.isBlank()) byClass
        else byClass.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
                    classMap[note.classId].orEmpty().contains(searchQuery, ignoreCase = true) ||
                    note.textContent.contains(searchQuery, ignoreCase = true)
        }
    }

    val filterSubjectName = filterClassId?.let { classMap[it] }

    // Group by subject name
    val groupedNotes = remember(filteredNotes, classMap) {
        filteredNotes.groupBy { classMap[it.classId] ?: "Unknown Class" }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RoundedTopHeaderPanel(
                title = if (filterSubjectName != null) "$filterSubjectName Notes" else "Class Notes",
                actionIcon = Icons.Default.Add,
                actionContentDescription = "Add Note",
                onActionClick = onAddNote
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
            // ── Search bar ────────────────────────────────────
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search notes or subject...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }

            // ── Stats chip ────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoteStatChip(
                        modifier = Modifier.weight(1f),
                        value = allNotes.size.toString(),
                        label = "Total Notes",
                        color = MaterialTheme.colorScheme.primary
                    )
                    NoteStatChip(
                        modifier = Modifier.weight(1f),
                        value = classMap.values.distinct().size.toString(),
                        label = "Subjects",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    NoteStatChip(
                        modifier = Modifier.weight(1f),
                        value = allNotes.count {
                            NoteAttachmentHelper.parseJsonArray(it.imagePaths).isNotEmpty() ||
                                    NoteAttachmentHelper.parseJsonArray(it.filePaths).isNotEmpty()
                        }.toString(),
                        label = "With Files",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // ── Empty state ───────────────────────────────────
            if (groupedNotes.isEmpty()) {
                item {
                    DashEmptyCard(
                        if (searchQuery.isNotEmpty()) "No notes match \"$searchQuery\""
                        else if (filterSubjectName != null) "No notes for $filterSubjectName yet. Tap + to add one!"
                        else "No notes yet. Tap + to add your first note!"
                    )
                }
            }

            // ── Grouped notes ─────────────────────────────────
            groupedNotes.forEach { (subjectName, notes) ->
                item {
                    Text(
                        text = subjectName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        subjectName = subjectName,
                        onEdit = { onEditNote(note.id) },
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Note Card ─────────────────────────────────────────────────────────────────
@Composable
private fun NoteCard(
    note: ClassNote,
    subjectName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    val checklistCount = NoteAttachmentHelper.checklistItemCount(note.checklistJson)
    val imageCount = NoteAttachmentHelper.parseJsonArray(note.imagePaths).size
    val fileCount = NoteAttachmentHelper.parseJsonArray(note.filePaths).size

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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Note icon ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.StickyNote2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Content ───────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = note.title.ifEmpty { "Untitled Note" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (note.textContent.isNotEmpty()) {
                    Text(
                        text = note.textContent,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ── Attachment badges ─────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (checklistCount > 0) {
                        AttachmentBadge(
                            icon = Icons.Default.CheckBox,
                            label = "$checklistCount item${if (checklistCount > 1) "s" else ""}",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (imageCount > 0) {
                        AttachmentBadge(
                            icon = Icons.Default.Image,
                            label = "$imageCount image${if (imageCount > 1) "s" else ""}",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (fileCount > 0) {
                        AttachmentBadge(
                            icon = Icons.Default.AttachFile,
                            label = "$fileCount file${if (fileCount > 1) "s" else ""}",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(note.updatedAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }

            // ── Actions ───────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
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
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            RoundedCornerShape(8.dp)
                        ),
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
            title = { Text("Delete Note", fontWeight = FontWeight.Medium) },
            text = {
                Text(
                    "Delete \"${note.title.ifEmpty { "Untitled Note" }}\"?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Attachment badge ──────────────────────────────────────────────────────────
@Composable
private fun AttachmentBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
            Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun NoteStatChip(
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
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = color)
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
