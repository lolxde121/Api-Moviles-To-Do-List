package com.upchiapas.todo_app.tasks.data.repository

import com.upchiapas.todo_app.core.network.NetworkErrorMapper
import com.upchiapas.todo_app.core.network.mapFailure
import com.upchiapas.todo_app.tasks.data.datasource.TasksApiService
import com.upchiapas.todo_app.tasks.data.dto.CreateTaskDto
import com.upchiapas.todo_app.tasks.data.dto.UpdateTaskDto
import com.upchiapas.todo_app.tasks.data.mapper.toDomain
import com.upchiapas.todo_app.tasks.data.mapper.toDomainList
import com.upchiapas.todo_app.tasks.data.model.Task

class TasksRepositoryImpl(
    private val apiService: TasksApiService
) : TasksRepository {

    override suspend fun getTasks(
        completed: Boolean?,
        skip: Int,
        limit: Int
    ): Result<List<Task>> {
        return runCatching {
            apiService.getTasks(completed, skip, limit).toDomainList()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun createTask(
        title: String,
        description: String?
    ): Result<Task> {
        return runCatching {
            val dto = CreateTaskDto(title = title, description = description, completed = false)
            apiService.createTask(dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun updateTaskStatus(
        id: Int,
        completed: Boolean
    ): Result<Task> {
        return runCatching {
            val dto = UpdateTaskDto(completed = completed)
            apiService.updateTask(id, dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun updateTask(
        id: Int,
        title: String?,
        description: String?,
        completed: Boolean?
    ): Result<Task> {
        return runCatching {
            val dto = UpdateTaskDto(title = title, description = description, completed = completed)
            apiService.updateTask(id, dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        return runCatching {
            val response = apiService.deleteTask(id)
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
            Unit
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }
}
