package com.upchiapas.todo_app.tasks.presentation.screens

import com.upchiapas.todo_app.tasks.data.model.Task

enum class TaskFilter(val displayName: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    COMPLETED("Completadas")
}

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val filter: TaskFilter = TaskFilter.ALL,
    val showCreateDialog: Boolean = false,
    val editingTask: Task? = null,
    val isSubmitting: Boolean = false
)
