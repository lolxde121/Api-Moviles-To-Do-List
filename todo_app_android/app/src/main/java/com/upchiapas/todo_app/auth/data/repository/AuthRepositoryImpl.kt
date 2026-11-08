package com.upchiapas.todo_app.auth.data.repository

import com.upchiapas.todo_app.auth.data.datasource.AuthApiService
import com.upchiapas.todo_app.auth.data.dto.RegisterRequestDto
import com.upchiapas.todo_app.auth.data.mapper.toDomain
import com.upchiapas.todo_app.auth.data.model.User
import com.upchiapas.todo_app.core.network.NetworkErrorMapper
import com.upchiapas.todo_app.core.network.TokenManager
import com.upchiapas.todo_app.core.network.mapFailure

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(email: String, password: String): Result<User> {
        return runCatching {
            val dto = RegisterRequestDto(email = email, password = password)
            apiService.register(dto).toDomain()
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return runCatching {
            val tokenDto = apiService.login(email = email, pass = password)
            tokenManager.saveToken(tokenDto.accessToken)
            tokenDto.accessToken
        }.mapFailure { throwable ->
            NetworkErrorMapper.map(throwable)
        }
    }

    override fun logout() {
        tokenManager.clearToken()
    }

    override fun isAuthenticated(): Boolean {
        return tokenManager.hasToken()
    }
}
