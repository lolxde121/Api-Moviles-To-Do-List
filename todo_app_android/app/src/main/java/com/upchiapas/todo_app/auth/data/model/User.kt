package com.upchiapas.todo_app.auth.data.model

/**
 * Entidad de dominio pura que representa al Usuario autenticado.
 */
data class User(
    val id: Int,
    val email: String,
    val createdAt: String
)
