package com.upchiapas.todo_app.tasks.data.mapper

import com.upchiapas.todo_app.tasks.data.dto.TaskDto
import com.upchiapas.todo_app.tasks.data.model.Task

/**
 * Funciones de mapeo entre DTOs y Modelo de Dominio de Tareas.
 * Equivalente a PostMapper.kt en el repositorio de referencia kt_p4a.
 */
fun TaskDto.toDomain(): Task {
    return Task(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        completed = this.completed,
        userId = this.userId,
        createdAt = this.createdAt
    )
}

fun List<TaskDto>.toDomainList(): List<Task> {
    return this.map { it.toDomain() }
}
