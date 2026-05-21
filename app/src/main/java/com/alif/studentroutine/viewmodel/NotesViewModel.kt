package com.alif.studentroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.NoteAttachmentHelper
import com.alif.studentroutine.data.entity.ClassNote
import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChecklistItem(
    val text: String,
    val isDone: Boolean
)

class NotesViewModel(private val repository: RoutineRepository) : ViewModel() {

    // --- All notes (for NotesScreen) ---
    val allNotes: StateFlow<List<ClassNote>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Notes filtered by class (for per-class view if needed later) ---
    fun getNotesForClass(classId: Int): Flow<List<ClassNote>> =
        repository.getNotesByClassId(classId)

    // --- Add/Edit form UI state ---
    private val _selectedClassId = MutableStateFlow<Int?>(null)
    val selectedClassId: StateFlow<Int?> = _selectedClassId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _textContent = MutableStateFlow("")
    val textContent: StateFlow<String> = _textContent.asStateFlow()

    private val _checklist = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val checklist: StateFlow<List<ChecklistItem>> = _checklist.asStateFlow()

    private val _imagePaths = MutableStateFlow<List<String>>(emptyList())
    val imagePaths: StateFlow<List<String>> = _imagePaths.asStateFlow()

    private val _filePaths = MutableStateFlow<List<String>>(emptyList())
    val filePaths: StateFlow<List<String>> = _filePaths.asStateFlow()

    // --- Form field updaters ---
    fun setSelectedClassId(classId: Int) { _selectedClassId.value = classId }
    fun setTitle(value: String) { _title.value = value }
    fun setTextContent(value: String) { _textContent.value = value }

    fun addChecklistItem(text: String) {
        _checklist.value = _checklist.value + ChecklistItem(text, isDone = false)
    }

    fun toggleChecklistItem(index: Int) {
        _checklist.value = _checklist.value.toMutableList().also {
            val item = it[index]
            it[index] = item.copy(isDone = !item.isDone)
        }
    }

    fun removeChecklistItem(index: Int) {
        _checklist.value = _checklist.value.toMutableList().also { it.removeAt(index) }
    }

    fun addImagePath(path: String) {
        _imagePaths.value = _imagePaths.value + path
    }

    fun removeImagePath(path: String) {
        NoteAttachmentHelper.deleteFile(path)
        _imagePaths.value = _imagePaths.value - path
    }

    fun addFilePath(path: String) {
        _filePaths.value = _filePaths.value + path
    }

    fun removeFilePath(path: String) {
        NoteAttachmentHelper.deleteFile(path)
        _filePaths.value = _filePaths.value - path
    }

    // --- Load existing note into form (for editing) ---
    fun loadNote(note: ClassNote) {
        _selectedClassId.value = note.classId
        _title.value = note.title
        _textContent.value = note.textContent
        _imagePaths.value = NoteAttachmentHelper.parseJsonArray(note.imagePaths)
        _filePaths.value = NoteAttachmentHelper.parseJsonArray(note.filePaths)
        _checklist.value = parseChecklist(note.checklistJson)
    }

    // --- Reset form (after save or on new note) ---
    fun resetForm() {
        _selectedClassId.value = null
        _title.value = ""
        _textContent.value = ""
        _checklist.value = emptyList()
        _imagePaths.value = emptyList()
        _filePaths.value = emptyList()
    }

    // --- Save (insert or update) ---
    fun saveNote(existingNoteId: Int? = null) {
        val classId = _selectedClassId.value ?: return
        viewModelScope.launch {
            val note = ClassNote(
                id = existingNoteId ?: 0,
                classId = classId,
                title = _title.value.trim(),
                textContent = _textContent.value.trim(),
                checklistJson = serializeChecklist(_checklist.value),
                imagePaths = serializeJsonArray(_imagePaths.value),
                filePaths = serializeJsonArray(_filePaths.value),
                createdAt = if (existingNoteId == null) System.currentTimeMillis()
                else repository.getNoteById(existingNoteId)?.createdAt
                    ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            if (existingNoteId == null) repository.insertNote(note)
            else repository.updateNote(note)
            resetForm()
        }
    }

    // --- Delete ---
    fun deleteNote(note: ClassNote) {
        viewModelScope.launch {
            NoteAttachmentHelper.deleteNoteAttachments(note)
            repository.deleteNote(note)
        }
    }

    // --- JSON helpers (no extra library needed) ---
    private fun serializeJsonArray(list: List<String>): String {
        return "[" + list.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" } + "]"
    }

    private fun parseJsonArray(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }
    }

    private fun serializeChecklist(items: List<ChecklistItem>): String {
        return "[" + items.joinToString(",") {
            "{\"text\":\"${it.text.replace("\"", "\\\"")}\",\"isDone\":${it.isDone}}"
        } + "]"
    }

    private fun parseChecklist(json: String): List<ChecklistItem> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(Regex("\\},\\s*\\{")).map { chunk ->
            val clean = chunk.replace("{", "").replace("}", "")
            val text = Regex("\"text\":\"(.*?)\"").find(clean)?.groupValues?.get(1) ?: ""
            val isDone = Regex("\"isDone\":(true|false)").find(clean)?.groupValues?.get(1) == "true"
            ChecklistItem(text, isDone)
        }
    }

    class Factory(private val repository: RoutineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(repository) as T
        }
    }
}