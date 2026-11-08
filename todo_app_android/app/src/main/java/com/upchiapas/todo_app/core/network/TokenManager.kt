package com.upchiapas.todo_app.core.network

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor seguro de persistencia local del token JWT de autenticación.
 * Aplica el principio de Responsabilidad Única (SRP).
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "todo_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }
}
