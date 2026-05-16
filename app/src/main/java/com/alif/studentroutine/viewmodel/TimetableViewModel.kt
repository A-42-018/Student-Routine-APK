package com.alif.studentroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimetableViewModel(private val repository: RoutineRepository) : ViewModel() {

    val allClasses: StateFlow<List<ClassItem>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteClass(classItem: ClassItem) {
        viewModelScope.launch {
            repository.deleteClass(classItem)
        }
    }

    class Factory(private val repository: RoutineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimetableViewModel(repository) as T
        }
    }
}
