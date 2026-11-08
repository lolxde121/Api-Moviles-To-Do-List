package com.upchiapas.todo_app.tasks.data.repository

import com.upchiapas.todo_app.tasks.data.model.Task

/**
 * Contrato del Repositorio de Tareas.
 * Aplica los principios ISP y DIP para mantener el desacoplamiento con la capa de presentación.
 */
interface TasksRepository {
    suspend fun getTasks(completed: Boolean? = null, skip: Int = 0, limit: Int = 50): Result<List<Task>>
    suspend fun createTask(title: String, description: String?): Result<Task>
    suspend fun updateTaskStatus(id: Int, completed: Boolean): Result<Task>
    suspend fun updateTask(id: Int, title: String?, description: String?, completed: Boolean?): Result<Task>
    suspend fun deleteTask(id: Int): Result<Unit>
}
