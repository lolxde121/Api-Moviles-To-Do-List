package com.upchiapas.todo_app.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que inyecta automáticamente el token JWT en las cabeceras HTTP:
 * Authorization: Bearer <token>
 * 
 * Cumple con el principio Open/Closed (OCP) al extender el pipeline de red sin modificar las peticiones individuales.
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si ya contiene cabecera Authorization o no hay token guardado, continuar
        val token = tokenManager.getToken()
        if (token.isNullOrBlank() || originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest)
        }

        // Inyectar el Bearer Token en la cabecera
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
