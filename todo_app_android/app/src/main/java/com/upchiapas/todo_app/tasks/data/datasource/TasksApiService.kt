package com.upchiapas.todo_app.tasks.data.datasource

import com.upchiapas.todo_app.tasks.data.dto.CreateTaskDto
import com.upchiapas.todo_app.tasks.data.dto.TaskDto
import com.upchiapas.todo_app.tasks.data.dto.UpdateTaskDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TasksApiService {

    @GET("tareas")
    suspend fun getTasks(
        @Query("completed") completed: Boolean? = null,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): List<TaskDto>

    @GET("tareas/{id}")
    suspend fun getTaskById(@Path("id") id: Int): TaskDto

    @POST("tareas")
    suspend fun createTask(@Body task: CreateTaskDto): TaskDto

    @PUT("tareas/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body task: UpdateTaskDto
    ): TaskDto

    @DELETE("tareas/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>
}
