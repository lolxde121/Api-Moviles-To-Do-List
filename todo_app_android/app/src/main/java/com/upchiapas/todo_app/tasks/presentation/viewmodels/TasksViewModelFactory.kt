package com.upchiapas.todo_app.tasks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upchiapas.todo_app.tasks.data.repository.TasksRepository

class TasksViewModelFactory(
    private val repository: TasksRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            return TasksViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
