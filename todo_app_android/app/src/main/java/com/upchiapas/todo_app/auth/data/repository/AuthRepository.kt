package com.upchiapas.todo_app.auth.data.repository

import com.upchiapas.todo_app.auth.data.model.User

/**
 * Contrato de repositorio de Autenticación.
 * Principio ISP (Interface Segregation Principle) y DIP (Dependency Inversion Principle).
 */
interface AuthRepository {
    suspend fun register(email: String, password: String): Result<User>
    suspend fun login(email: String, password: String): Result<String>
    fun logout()
    fun isAuthenticated(): Boolean
}
