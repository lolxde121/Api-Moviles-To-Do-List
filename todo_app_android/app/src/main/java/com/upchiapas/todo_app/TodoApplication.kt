package com.upchiapas.todo_app

import android.app.Application
import com.upchiapas.todo_app.di.AppModule

/**
 * Clase Application personalizada para inicializar y proveer el contenedor de dependencias (AppModule).
 * Idéntico a KtApplication.kt del repositorio de referencia kt_p4a.
 */
class TodoApplication : Application() {

    lateinit var appModule: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule(this)
    }
}
