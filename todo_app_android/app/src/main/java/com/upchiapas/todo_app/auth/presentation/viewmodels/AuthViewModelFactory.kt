package com.upchiapas.todo_app.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.upchiapas.todo_app.auth.data.repository.AuthRepository

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
