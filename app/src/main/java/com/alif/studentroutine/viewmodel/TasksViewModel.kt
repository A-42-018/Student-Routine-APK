package com.alif.studentroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.entity.TaskItem
import android.content.Context
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.widget.WidgetUpdater
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: RoutineRepository) : ViewModel() {

    val allTasks: StateFlow<List<TaskItem>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleTaskComplete(context: Context, task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
            WidgetUpdater.updateAll(context)
        }
    }

    fun deleteTask(context: Context, task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            WidgetUpdater.updateAll(context)
        }
    }

    class Factory(private val repository: RoutineRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(repository) as T
        }
    }
}
