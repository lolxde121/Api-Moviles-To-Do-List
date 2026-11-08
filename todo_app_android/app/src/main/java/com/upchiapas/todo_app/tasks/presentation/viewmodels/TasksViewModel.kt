package com.upchiapas.todo_app.tasks.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.todo_app.tasks.data.model.Task
import com.upchiapas.todo_app.tasks.data.repository.TasksRepository
import com.upchiapas.todo_app.tasks.presentation.screens.TaskFilter
import com.upchiapas.todo_app.tasks.presentation.screens.TasksUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val repository: TasksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        val currentFilter = _uiState.value.filter
        val completedParam: Boolean? = when (currentFilter) {
            TaskFilter.ALL -> null
            TaskFilter.PENDING -> false
            TaskFilter.COMPLETED -> true
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getTasks(completed = completedParam)
            result.fold(
                onSuccess = { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Error al cargar las tareas"
                        )
                    }
                }
            )
        }
    }

    fun setFilter(filter: TaskFilter) {
        if (_uiState.value.filter != filter) {
            _uiState.update { it.copy(filter = filter) }
            fetchTasks()
        }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createTask(title: String, description: String?) {
        _uiState.update { it.copy(isSubmitting = true, showCreateDialog = false) }
        viewModelScope.launch {
            val result = repository.createTask(title, description)
            result.fold(
                onSuccess = { newTask ->
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            tasks = listOf(newTask) + state.tasks,
                            userMessage = "Tarea creada con éxito."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            userMessage = "Error al crear la tarea: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun toggleTaskStatus(task: Task) {
        val newStatus = !task.completed
        viewModelScope.launch {
            val result = repository.updateTaskStatus(task.id, newStatus)
            result.fold(
                onSuccess = { updatedTask ->
                    _uiState.update { state ->
                        val updatedList = state.tasks.map {
                            if (it.id == updatedTask.id) updatedTask else it
                        }
                        state.copy(
                            tasks = updatedList,
                            userMessage = if (newStatus) "¡Tarea completada!" else "Tarea marcada como pendiente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "No se pudo actualizar el estado: ${error.localizedMessage}")
                    }
                }
            )
        }
    }

    fun openEditDialog(task: Task) {
        _uiState.update { it.copy(editingTask = task) }
    }

    fun closeEditDialog() {
        _uiState.update { it.copy(editingTask = null) }
    }

    fun updateTask(id: Int, title: String, description: String?) {
        _uiState.update { it.copy(isSubmitting = true, editingTask = null) }
        viewModelScope.launch {
            val result = repository.updateTask(id, title, description, null)
            result.fold(
                onSuccess = { updatedTask ->
                    _uiState.update { state ->
                        val updatedList = state.tasks.map {
                            if (it.id == updatedTask.id) updatedTask else it
                        }
                        state.copy(
                            isSubmitting = false,
                            tasks = updatedList,
                            userMessage = "Tarea actualizada exitosamente."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            userMessage = "Error al actualizar la tarea: ${error.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val result = repository.deleteTask(task.id)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            tasks = state.tasks.filterNot { it.id == task.id },
                            userMessage = "Tarea eliminada correctamente."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Error al eliminar la tarea: ${error.localizedMessage}")
                    }
                }
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
