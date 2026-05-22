package com.alif.studentroutine.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.widget.WidgetUpdater
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimetableViewModel(private val repository: RoutineRepository) : ViewModel() {

    val allClasses: StateFlow<List<ClassItem>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteClass(context: Context, classItem: ClassItem) {
        viewModelScope.launch {
            repository.deleteClass(classItem)
            WidgetUpdater.updateAll(context)
        }
    }

    class Factory(private val repository: RoutineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimetableViewModel(repository) as T
        }
    }
}
