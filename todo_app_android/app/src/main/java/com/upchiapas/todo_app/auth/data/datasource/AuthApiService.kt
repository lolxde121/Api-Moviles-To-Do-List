package com.upchiapas.todo_app.auth.data.datasource

import com.upchiapas.todo_app.auth.data.dto.RegisterRequestDto
import com.upchiapas.todo_app.auth.data.dto.TokenDto
import com.upchiapas.todo_app.auth.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): UserDto

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") pass: String
    ): TokenDto
}
