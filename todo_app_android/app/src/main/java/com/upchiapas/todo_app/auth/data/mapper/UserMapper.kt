package com.upchiapas.todo_app.auth.data.mapper

import com.upchiapas.todo_app.auth.data.dto.UserDto
import com.upchiapas.todo_app.auth.data.model.User

/**
 * Funciones de extensión de mapeo para desacoplar DTOs de Modelos de Dominio.
 * Principio SRP (Single Responsibility Principle).
 */
fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        createdAt = this.createdAt
    )
}
