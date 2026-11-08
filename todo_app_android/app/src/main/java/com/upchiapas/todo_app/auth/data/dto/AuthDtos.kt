package com.upchiapas.todo_app.auth.data.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class TokenDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)
