# To-Do Mobile App (Android - Kotlin - Jetpack Compose)

Aplicación móvil nativa para Android desarrollada en **Kotlin** con **Jetpack Compose (Material 3)**, diseñada para consumir de forma segura la API REST de Lista de Tareas (*To-Do List*) con autenticación JWT.

La arquitectura de este proyecto toma como referencia directa el repositorio [`https://github.com/alilopez37/kt_p4a.git`](https://github.com/alilopez37/kt_p4a.git), aplicando estrictamente el **patrón arquitectónico MVVM** y los **principios de diseño SOLID**.

---

## 1. Principios SOLID Aplicados

| Principio | Aplicación en el Proyecto |
| :--- | :--- |
| **S - Single Responsibility Principle (SRP)** | Cada componente tiene una única razón para cambiar:<ul><li>`TasksApiService` / `AuthApiService`: Solo definen los contratos HTTP de Retrofit.</li><li>`TaskMapper` / `UserMapper`: Solo transforman datos entre DTOs y Modelos de Dominio.</li><li>`TokenManager`: Solo gestiona la persistencia segura del token JWT en `SharedPreferences`.</li><li>`TasksViewModel` / `AuthViewModel`: Solo manejan el estado de la UI (`UiState`) y la lógica de presentación.</li><li>`TasksContent` / `AuthContent`: Son composables sin estado (*stateless*) dedicados exclusivamente a renderizar la vista.</li></ul> |
| **O - Open/Closed Principle (OCP)** | La arquitectura está abierta a extensión y cerrada a modificación:<ul><li>`AuthInterceptor` intercepta y enriquece las solicitudes HTTP con el token Bearer sin modificar las llamadas individuales a la API.</li><li>Los repositorios permiten agregar caching local con Room o fuentes alternativas sin requerir cambios en los `ViewModel`.</li></ul> |
| **L - Liskov Substitution Principle (LSP)** | Las implementaciones `TasksRepositoryImpl` y `AuthRepositoryImpl` implementan fielmente las interfaces `TasksRepository` y `AuthRepository`. Cualquier implementación alternativa (como un `MockTasksRepository` para pruebas unitarias) puede sustituir a la real sin romper el comportamiento de la app. |
| **I - Interface Segregation Principle (ISP)** | Se separaron los contratos en interfaces pequeñas y específicas (`AuthRepository` y `TasksRepository`), evitando una interfaz monolítica de repositorio con métodos innecesarios para cada caso de uso. |
| **D - Dependency Inversion Principle (DIP)** | Los `ViewModel` dependen de abstracciones (interfaces `TasksRepository` y `AuthRepository`) y no de implementaciones concretas ni de Retrofit directamente. Las dependencias se crean e inyectan a través del contenedor `AppModule` instanciado en `TodoApplication`. |

---

## 2. Patrón Arquitectónico MVVM

El flujo unidireccional de datos (*UDF - Unidirectional Data Flow*) se estructura de la siguiente manera:

```text
[ Remote API (FastAPI) ]
           ▲
           │ HTTP / JWT Bearer
           ▼
[ Data Layer: ApiService + DTOs ]
           │
           │ Mappers (toDomain)
           ▼
[ Domain Layer: Repositories (Interfaces + Impls) + Models ]
           │
           │ Result<T> / Coroutines
           ▼
[ Presentation Layer: ViewModel (MutableStateFlow) ]
           │
           │ StateFlow<UiState>
           ▼
[ View Layer: Jetpack Compose (Stateful & Stateless Composable) ]
```

### Componentes Clave:
1. **Model & Data**:
   - `TaskDto`, `UserDto`: Modelan exactamente la respuesta JSON de la API.
   - `Task`, `User`: Modelos inmutables de dominio.
   - `TaskMapper`: Mapea `TaskDto` a `Task` y listas con `toDomainList()`.
2. **ViewModel**:
   - Hereda de `androidx.lifecycle.ViewModel`.
   - Expone un `StateFlow<TasksUiState>` inmutable hacia la vista.
   - Maneja operaciones asíncronas con `viewModelScope.launch` y la mónada `Result<T>` (`fold`, `onSuccess`, `onFailure`).
3. **View**:
   - **Stateful Composable (`TasksPage`)**: Se conecta al ciclo de vida del ViewModel mediante `collectAsStateWithLifecycle()`.
   - **Stateless Composable (`TasksContent`)**: Recibe únicamente el estado (`uiState`) y callbacks de eventos (`onRefresh`, `onCreateTask`, `onDeleteTask`), facilitando previsualizaciones en Android Studio y pruebas de UI.

---

## 3. Estructura de Directorios

```text
todo_app_android/app/src/main/java/com/upchiapas/todo_app/
├── TodoApplication.kt                 # Application class con AppModule
├── MainActivity.kt                    # Punto de entrada y orquestador de navegación
├── core/
│   └── network/
│       ├── AuthInterceptor.kt         # Inyector automático de Authorization: Bearer <token>
│       ├── NetworkErrorMapper.kt      # Mapeo de excepciones HTTP y extensión Result.mapFailure
│       └── TokenManager.kt            # Persistencia de access_token en SharedPreferences
├── di/
│   └── AppModule.kt                   # Service Locator / Dependency Injection Module
├── auth/
│   ├── data/
│   │   ├── datasource/AuthApiService.kt
│   │   ├── dto/AuthDtos.kt
│   │   ├── mapper/UserMapper.kt
│   │   ├── model/User.kt
│   │   └── repository/
│   │       ├── AuthRepository.kt
│   │       └── AuthRepositoryImpl.kt
│   └── presentation/
│       ├── screens/
│       │   ├── AuthPage.kt
│       │   └── AuthUiState.kt
│       └── viewmodels/
│           ├── AuthViewModel.kt
│           └── AuthViewModelFactory.kt
├── tasks/
│   ├── data/
│   │   ├── datasource/TasksApiService.kt
│   │   ├── dto/TaskDtos.kt
│   │   ├── mapper/TaskMapper.kt
│   │   ├── model/Task.kt
│   │   └── repository/
│   │       ├── TasksRepository.kt
│   │       └── TasksRepositoryImpl.kt
│   └── presentation/
│       ├── components/
│       │   ├── CreateTaskDialog.kt
│       │   ├── EditTaskDialog.kt
│       │   ├── TaskFilterChips.kt
│       │   └── TaskItem.kt
│       ├── screens/
│       │   ├── TasksPage.kt
│       │   └── TasksUiState.kt
│       └── viewmodels/
│           ├── TasksViewModel.kt
│           └── TasksViewModelFactory.kt
└── ui/
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 4. Configuración de Red para Conectar con la API

En el archivo [`AppModule.kt`](file:///home/edgar/Descargas/Api-moviles/todo_app_android/app/src/main/java/com/upchiapas/todo_app/di/AppModule.kt):

```kotlin
private val baseUrl = "http://10.0.2.2:8000/"
```

- **Emulador Oficial de Android Studio**: Utiliza `http://10.0.2.2:8000/` (apunta automáticamente al `localhost:8000` de tu máquina host).
- **Dispositivo Físico conectado por WiFi**: Cambia la URL por la IP local de tu computadora en la red (ejemplo: `http://192.168.1.75:8000/`).
- **Servidor en la Nube (AWS EC2 / Producción)**: Coloca el dominio público (ejemplo: `https://api.tudominio.com/`).

> **Nota**: El archivo `AndroidManifest.xml` ya incluye `android:usesCleartextTraffic="true"` para permitir peticiones HTTP sin cifrar en entornos de desarrollo local.

---

## 5. Instrucciones para Ejecutar en Android Studio

1. Abre **Android Studio**.
2. Selecciona **File -> Open...** y elige la carpeta `todo_app_android`.
3. Espera a que Gradle sincronice las dependencias del proyecto.
4. Asegúrate de tener levantada la API REST de FastAPI en la máquina host:
   ```bash
   cd ..
   uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```
5. En Android Studio, presiona **Run 'app'** (`Shift + F10`) seleccionando un emulador o dispositivo físico.
