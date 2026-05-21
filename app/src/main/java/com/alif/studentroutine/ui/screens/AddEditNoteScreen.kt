package com.alif.studentroutine.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alif.studentroutine.data.NoteAttachmentHelper
import com.alif.studentroutine.data.NoteImagePreview
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.viewmodel.DashboardViewModel
import com.alif.studentroutine.viewmodel.NotesViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    repository: RoutineRepository,
    noteId: Int?,
    preselectedClassId: Int?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory(repository))
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))

    val allClasses by dashboardViewModel.allClasses.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val textContent by viewModel.textContent.collectAsStateWithLifecycle()
    val checklist by viewModel.checklist.collectAsStateWithLifecycle()
    val imagePaths by viewModel.imagePaths.collectAsStateWithLifecycle()
    val filePaths by viewModel.filePaths.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()

    var newChecklistText by remember { mutableStateOf("") }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var loadedNote by remember { mutableStateOf<ClassNote?>(null) }
    var previewImagePath by remember { mutableStateOf<String?>(null) }

    // Load existing note or preselect class once
    LaunchedEffect(noteId, preselectedClassId) {
        if (!isLoaded) {
            if (noteId != null) {
                val note = repository.getNoteById(noteId)
                note?.let {
                    loadedNote = it
                    viewModel.loadNote(it)
                }
            } else if (preselectedClassId != null) {
                viewModel.setSelectedClassId(preselectedClassId)
            }
            isLoaded = true
        }
    }

    // Image picker
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val internalPath = copyFileToInternal(context, it, "images")
            if (internalPath != null) viewModel.addImagePath(internalPath)
        }
    }

    // File picker
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val internalPath = copyFileToInternal(context, it, "files")
            if (internalPath != null) viewModel.addFilePath(internalPath)
        }
    }

    val selectedClassName = allClasses.find { it.id == selectedClassId }?.subject ?: "Select Class"
    val isEditing = noteId != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Note" else "New Note",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.resetForm(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.saveNote(existingNoteId = noteId)
                            onBack()
                        },
                        enabled = selectedClassId != null && title.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedClassId != null && title.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {

            // ── Class picker ──────────────────────────────────
            item {
                SectionLabel("Class")
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClassName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        allClasses.forEach { classItem ->
                            DropdownMenuItem(
                                text = { Text(classItem.subject) },
                                onClick = {
                                    viewModel.setSelectedClassId(classItem.id)
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ── Title ─────────────────────────────────────────
            item {
                SectionLabel("Title")
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.setTitle(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Note title...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // ── Text content ──────────────────────────────────
            item {
                SectionLabel("Notes")
                OutlinedTextField(
                    value = textContent,
                    onValueChange = { viewModel.setTextContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = {
                        Text(
                            "Write your notes here...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    maxLines = 10,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // ── Checklist ─────────────────────────────────────
            item {
                SectionLabel("Checklist")
            }

            itemsIndexed(checklist) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { viewModel.toggleChecklistItem(index) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = item.text,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        color = if (item.isDone)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { viewModel.removeChecklistItem(index) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newChecklistText,
                        onValueChange = { newChecklistText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Add checklist item...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    FilledIconButton(
                        onClick = {
                            if (newChecklistText.isNotBlank()) {
                                viewModel.addChecklistItem(newChecklistText.trim())
                                newChecklistText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add item")
                    }
                }
            }

            // ── Images ────────────────────────────────────────
            item {
                SectionLabel("Images")
                AttachmentSection(
                    items = imagePaths,
                    icon = Icons.Default.Image,
                    addLabel = "Add Image",
                    onAdd = {
                        imageLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onOpen = { previewImagePath = it },
                    onRemove = { viewModel.removeImagePath(it) },
                    displayName = { it.substringAfterLast("/") }
                )
            }

            // ── Files ─────────────────────────────────────────
            item {
                SectionLabel("File Attachments")
                AttachmentSection(
                    items = filePaths,
                    icon = Icons.Default.AttachFile,
                    addLabel = "Add File",
                    onAdd = { fileLauncher.launch(arrayOf("*/*")) },
                    onOpen = { NoteAttachmentHelper.openAttachment(context, it, isImage = false) },
                    onRemove = { viewModel.removeFilePath(it) },
                    displayName = { it.substringAfterLast("/") }
                )
            }
        }
    }

    if (showDeleteDialog && loadedNote != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Delete Note", fontWeight = FontWeight.Medium) },
            text = {
                Text(
                    "Delete \"${loadedNote!!.title.ifEmpty { "Untitled Note" }}\"?",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(loadedNote!!)
                    viewModel.resetForm()
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    previewImagePath?.let { path ->
        AlertDialog(
            onDismissRequest = { previewImagePath = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text(path.substringAfterLast("/"), fontWeight = FontWeight.Medium) },
            text = { NoteImagePreview(path) },
            confirmButton = {
                TextButton(onClick = {
                    NoteAttachmentHelper.openAttachment(context, path, isImage = true)
                }) { Text("Open externally") }
            },
            dismissButton = {
                TextButton(onClick = { previewImagePath = null }) { Text("Close") }
            }
        )
    }
}

// ── Section label ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

// ── Attachment section ────────────────────────────────────────────────────────
@Composable
private fun AttachmentSection(
    items: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    addLabel: String,
    onAdd: () -> Unit,
    onOpen: ((String) -> Unit)? = null,
    onRemove: (String) -> Unit,
    displayName: (String) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { path ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onOpen != null) {
                            Modifier.clickable { onOpen(path) }
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = displayName(path),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
                IconButton(
                    onClick = { onRemove(path) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(6.dp))
            Text(addLabel, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── File copy helper ──────────────────────────────────────────────────────────
private fun copyFileToInternal(context: Context, uri: Uri, subfolder: String): String? {
    return try {
        val dir = File(context.filesDir, "notes/$subfolder").also { it.mkdirs() }
        val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment?.substringAfterLast("/") ?: "file"}"
        val dest = File(dir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        null
    }
}