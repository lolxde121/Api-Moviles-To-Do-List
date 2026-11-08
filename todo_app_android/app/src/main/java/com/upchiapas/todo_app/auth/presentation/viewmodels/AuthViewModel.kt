package com.upchiapas.todo_app.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.todo_app.auth.data.repository.AuthRepository
import com.upchiapas.todo_app.auth.presentation.screens.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkInitialAuthStatus()
    }

    private fun checkInitialAuthStatus() {
        if (repository.isAuthenticated()) {
            _uiState.update { it.copy(isAuthenticated = true) }
        }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
                userMessage = null
            )
        }
    }

    fun submit() {
        val currentState = _uiState.value
        val email = currentState.emailInput.trim()
        val password = currentState.passwordInput

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos.") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }

        if (currentState.isRegisterMode) {
            register(email, password)
        } else {
            login(email, password)
        }
    }

    private fun login(email: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            userMessage = "¡Bienvenido de nuevo!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    private fun register(email: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.register(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegisterMode = false,
                            userMessage = "Usuario ${user.email} registrado exitosamente. Por favor inicia sesión."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Error al registrar usuario"
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            AuthUiState(
                isAuthenticated = false,
                userMessage = "Sesión cerrada correctamente"
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
