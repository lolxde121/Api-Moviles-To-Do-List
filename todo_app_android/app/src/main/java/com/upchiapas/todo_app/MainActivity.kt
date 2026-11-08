package com.upchiapas.todo_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.todo_app.auth.presentation.screens.AuthPage
import com.upchiapas.todo_app.auth.presentation.viewmodels.AuthViewModel
import com.upchiapas.todo_app.tasks.presentation.screens.TasksPage
import com.upchiapas.todo_app.tasks.presentation.viewmodels.TasksViewModel
import com.upchiapas.todo_app.ui.theme.TodoAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appModule = (application as TodoApplication).appModule

        setContent {
            TodoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appModule = appModule)
                }
            }
        }
    }
}

/**
 * Orquestador principal de navegación entre las pantallas de Autenticación y Tareas.
 */
@Composable
fun AppNavigation(
    appModule: com.upchiapas.todo_app.di.AppModule,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel(factory = appModule.authViewModelFactory)
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (authUiState.isAuthenticated) {
        val tasksViewModel: TasksViewModel = viewModel(factory = appModule.tasksViewModelFactory)
        TasksPage(
            viewModel = tasksViewModel,
            onLogout = { authViewModel.logout() },
            modifier = modifier
        )
    } else {
        AuthPage(
            viewModel = authViewModel,
            modifier = modifier
        )
    }
}
