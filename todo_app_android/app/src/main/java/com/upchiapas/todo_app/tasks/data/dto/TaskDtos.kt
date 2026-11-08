package com.upchiapas.todo_app.tasks.data.dto

import com.google.gson.annotations.SerializedName

data class TaskDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("completed")
    val completed: Boolean,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class CreateTaskDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("completed")
    val completed: Boolean = false
)

data class UpdateTaskDto(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("completed")
    val completed: Boolean? = null
)
