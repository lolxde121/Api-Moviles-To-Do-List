package com.upchiapas.todo_app.di

import android.content.Context
import com.upchiapas.todo_app.auth.data.datasource.AuthApiService
import com.upchiapas.todo_app.auth.data.repository.AuthRepository
import com.upchiapas.todo_app.auth.data.repository.AuthRepositoryImpl
import com.upchiapas.todo_app.auth.presentation.viewmodels.AuthViewModelFactory
import com.upchiapas.todo_app.core.network.AuthInterceptor
import com.upchiapas.todo_app.core.network.TokenManager
import com.upchiapas.todo_app.tasks.data.datasource.TasksApiService
import com.upchiapas.todo_app.tasks.data.repository.TasksRepository
import com.upchiapas.todo_app.tasks.data.repository.TasksRepositoryImpl
import com.upchiapas.todo_app.tasks.presentation.viewmodels.TasksViewModelFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Módulo de Inyección de Dependencias manual (Service Locator / Container).
 * Estructura idéntica y compatible con JsonPlaceholderModule.kt del repositorio de referencia kt_p4a.
 * 
 * Cumple con el principio de Inversión de Dependencias (DIP) al centralizar la creación
 * y enlace de interfaces con sus implementaciones concretas.
 */
class AppModule(context: Context) {

    /**
     * URL base de la API FastAPI.
     * - Para emulador oficial de Android Studio: http://10.0.2.2:8000/
     * - Para dispositivo físico o red local: http://<TU_IP_LOCAL>:8000/
     * - Para producción AWS EC2: https://api.tudominio.com/
     */
    private val baseUrl = "http://10.0.2.2:8000/"

    val tokenManager: TokenManager by lazy {
        TokenManager(context.applicationContext)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // DataSources
    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    private val tasksApiService: TasksApiService by lazy {
        retrofit.create(TasksApiService::class.java)
    }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, tokenManager)
    }

    val tasksRepository: TasksRepository by lazy {
        TasksRepositoryImpl(tasksApiService)
    }

    // ViewModel Factories
    val authViewModelFactory: AuthViewModelFactory by lazy {
        AuthViewModelFactory(authRepository)
    }

    val tasksViewModelFactory: TasksViewModelFactory by lazy {
        TasksViewModelFactory(tasksRepository)
    }
}
