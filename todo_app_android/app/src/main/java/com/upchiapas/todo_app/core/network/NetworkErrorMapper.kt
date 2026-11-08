package com.upchiapas.todo_app.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

/**
 * Función de utilidad para mapear excepciones de red y HTTP a mensajes claros para el usuario,
 * siguiendo el patrón implementado en kt_p4a.
 */
object NetworkErrorMapper {

    fun map(throwable: Throwable): Throwable {
        return when (throwable) {
            is HttpException -> {
                val code = throwable.code()
                val errorBody = throwable.response()?.errorBody()?.string()
                val serverMessage = try {
                    if (!errorBody.isNullOrBlank()) {
                        val json = JsonParser.parseString(errorBody).asJsonObject
                        if (json.has("detail")) {
                            json.get("detail").asString
                        } else null
                    } else null
                } catch (_: Exception) {
                    null
                }

                val finalMessage = serverMessage ?: when (code) {
                    400 -> "Solicitud incorrecta o datos inválidos"
                    401 -> "Credenciales inválidas o sesión expirada"
                    404 -> "Recurso no encontrado"
                    500 -> "Error interno del servidor"
                    else -> "Error HTTP $code: ${throwable.message()}"
                }
                Exception(finalMessage)
            }
            is IOException -> Exception("Error de red: Comprueba tu conexión a internet o el estado de la API")
            else -> throwable
        }
    }
}

/**
 * Extensión para Result<T> idéntica a la del repositorio de referencia kt_p4a.
 */
fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )
}
