package com.upchiapas.todo_app.auth.presentation.screens

/**
 * Estado inmutable de la UI para la pantalla de Autenticación.
 */
data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val isAuthenticated: Boolean = false
)
