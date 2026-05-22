package com.alif.studentroutine.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alif.studentroutine.data.DataStoreManager
import com.alif.studentroutine.data.entity.ClassItem
import com.alif.studentroutine.data.entity.TaskItem
import com.alif.studentroutine.data.repository.RoutineRepository
import com.alif.studentroutine.notification.AlarmScheduler
import com.alif.studentroutine.widget.WidgetUpdater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditViewModel(
    private val repository: RoutineRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    suspend fun getClassById(id: Int): ClassItem? = repository.getClassById(id)
    suspend fun getTaskById(id: Int): TaskItem? = repository.getTaskById(id)

    fun saveClass(context: Context, classItem: ClassItem) {
        viewModelScope.launch {
            val id = repository.insertClass(classItem)
            val savedClass = classItem.copy(id = id.toInt())
            if (dataStoreManager.notificationsEnabledFlow.first()) {
                AlarmScheduler.scheduleClassReminder(context, savedClass)
            }
            WidgetUpdater.updateAll(context)
        }
    }

    fun updateClass(context: Context, classItem: ClassItem) {
        viewModelScope.launch {
            repository.updateClass(classItem)
            AlarmScheduler.cancelReminder(context, classItem.id, 10000)
            if (dataStoreManager.notificationsEnabledFlow.first()) {
                AlarmScheduler.scheduleClassReminder(context, classItem)
            }
            WidgetUpdater.updateAll(context)
        }
    }

    fun deleteClass(context: Context, classItem: ClassItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(context, classItem.id, 10000)
            repository.deleteClass(classItem)
            WidgetUpdater.updateAll(context)
        }
    }

    fun saveTask(context: Context, taskItem: TaskItem) {
        viewModelScope.launch {
            val id = repository.insertTask(taskItem)
            val savedTask = taskItem.copy(id = id.toInt())
            if (dataStoreManager.notificationsEnabledFlow.first()) {
                AlarmScheduler.scheduleTaskReminder(context, savedTask)
            }
            WidgetUpdater.updateAll(context)
        }
    }

    fun updateTask(context: Context, taskItem: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(taskItem)
            AlarmScheduler.cancelReminder(context, taskItem.id, 20000)
            if (!taskItem.isCompleted && dataStoreManager.notificationsEnabledFlow.first()) {
                AlarmScheduler.scheduleTaskReminder(context, taskItem)
            }
            WidgetUpdater.updateAll(context)
        }
    }

    fun deleteTask(context: Context, taskItem: TaskItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(context, taskItem.id, 20000)
            repository.deleteTask(taskItem)
            WidgetUpdater.updateAll(context)
        }
    }

    class Factory(
        private val repository: RoutineRepository,
        private val dataStoreManager: DataStoreManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddEditViewModel(repository, dataStoreManager) as T
        }
    }
}
