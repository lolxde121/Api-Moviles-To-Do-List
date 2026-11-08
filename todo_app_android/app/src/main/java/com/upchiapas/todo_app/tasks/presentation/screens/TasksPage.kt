package com.upchiapas.todo_app.tasks.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.todo_app.tasks.data.model.Task
import com.upchiapas.todo_app.tasks.presentation.components.CreateTaskDialog
import com.upchiapas.todo_app.tasks.presentation.components.EditTaskDialog
import com.upchiapas.todo_app.tasks.presentation.components.TaskFilterChips
import com.upchiapas.todo_app.tasks.presentation.components.TaskItem
import com.upchiapas.todo_app.tasks.presentation.viewmodels.TasksViewModel

/**
 * Stateful Composable: Conectado con la arquitectura y el ViewModel de Tareas.
 * Inspirado directamente en PostsPage.kt de kt_p4a.
 */
@Composable
fun TasksPage(
    viewModel: TasksViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        uiState = uiState,
        onRefresh = { viewModel.fetchTasks() },
        onFilterSelected = { viewModel.setFilter(it) },
        onOpenCreateDialog = { viewModel.openCreateDialog() },
        onCloseCreateDialog = { viewModel.closeCreateDialog() },
        onCreateTask = { title, desc -> viewModel.createTask(title, desc) },
        onToggleStatus = { viewModel.toggleTaskStatus(it) },
        onOpenEditDialog = { viewModel.openEditDialog(it) },
        onCloseEditDialog = { viewModel.closeEditDialog() },
        onUpdateTask = { id, title, desc -> viewModel.updateTask(id, title, desc) },
        onDeleteTask = { viewModel.deleteTask(it) },
        onClearUserMessage = { viewModel.clearUserMessage() },
        onLogout = onLogout,
        modifier = modifier
    )
}

/**
 * Stateless Composable: Vista pura para el listado y gestión de tareas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
    uiState: TasksUiState,
    onRefresh: () -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onCloseCreateDialog: () -> Unit,
    onCreateTask: (String, String?) -> Unit,
    onToggleStatus: (Task) -> Unit,
    onOpenEditDialog: (Task) -> Unit,
    onCloseEditDialog: () -> Unit,
    onUpdateTask: (Int, String, String?) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onClearUserMessage: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Actualizar tareas")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenCreateDialog) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Crear tarea")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TaskFilterChips(
                selectedFilter = uiState.filter,
                onFilterSelected = onFilterSelected
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }

                    uiState.errorMessage != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Reintentar")
                            }
                        }
                    }

                    uiState.tasks.isEmpty() -> {
                        Text(
                            text = "No tienes tareas en esta sección.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.tasks, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onToggleStatus = onToggleStatus,
                                    onEdit = onOpenEditDialog,
                                    onDelete = onDeleteTask
                                )
                            }
                        }
                    }
                }

                if (uiState.showCreateDialog) {
                    CreateTaskDialog(
                        onDismiss = onCloseCreateDialog,
                        onCreate = onCreateTask
                    )
                }

                if (uiState.editingTask != null) {
                    EditTaskDialog(
                        task = uiState.editingTask,
                        onDismiss = onCloseEditDialog,
                        onUpdate = onUpdateTask
                    )
                }

                if (uiState.isSubmitting) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
