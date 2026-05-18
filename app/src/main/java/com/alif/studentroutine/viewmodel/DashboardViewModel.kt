package com.alif.studentroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(private val repository: RoutineRepository) : ViewModel() {

    private val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    val todayClasses: StateFlow<List<ClassItem>> = repository.getClassesByDay(today)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allClasses: StateFlow<List<ClassItem>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingTasks: StateFlow<List<TaskItem>> = repository.getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markTaskComplete(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = true))
        }
    }

    class Factory(private val repository: RoutineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
