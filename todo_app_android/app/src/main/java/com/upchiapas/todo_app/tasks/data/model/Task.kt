package com.upchiapas.todo_app.tasks.data.model

/**
 * Entidad de dominio que representa una tarea individual en la capa de negocio.
 */
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean,
    val userId: Int,
    val createdAt: String
)
