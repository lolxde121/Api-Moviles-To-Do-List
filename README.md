# Documentación Técnica: To-Do List REST API

API REST completa, modular y escalable para una plataforma de gestión de tareas multiusuario (*To-Do List*) construida con **FastAPI**, **Pydantic v2**, **SQLAlchemy 2.0** y autenticación segura **OAuth2 con JWT**.

Diseñada para ser consumida de forma segura tanto desde aplicaciones móviles (**Android / iOS**) como desde clientes web modernos.

---

## Índice

1. [Arquitectura y Tecnologías](#1-arquitectura-y-tecnologías)
2. [Estructura del Proyecto](#2-estructura-del-proyecto)
3. [Modelos de Base de Datos (ORM)](#3-modelos-de-base-de-datos-orm)
4. [Esquemas de Datos (Pydantic v2)](#4-esquemas-de-datos-pydantic-v2)
5. [Contratos de la API (Endpoints)](#5-contratos-de-la-api-endpoints)
   - [5.1 Health Check](#51-health-check)
   - [5.2 Autenticación](#52-autenticación)
   - [5.3 Tareas (CRUD Principal)](#53-tareas-crud-principal)
6. [Manejo de Errores y Códigos de Estado](#6-manejo-de-errores-y-códigos-de-estado)
7. [Variables de Entorno](#7-variables-de-entorno)
8. [Instalación y Ejecución Local](#8-instalación-y-ejecución-local)
9. [Consumo desde Clientes Móviles](#9-consumo-desde-clientes-móviles)
10. [Despliegue en AWS EC2](#10-despliegue-en-aws-ec2)

---

## 1. Arquitectura y Tecnologías

| Componente | Tecnología / Librería | Descripción |
| :--- | :--- | :--- |
| **Lenguaje** | Python 3.10+ | Lenguaje base tipado |
| **Framework Web** | FastAPI 0.111+ | Framework asíncrono de alto rendimiento con validación automática |
| **Validación de Esquemas** | Pydantic v2 | Serialización y validación estricta de esquemas de datos |
| **Servidor ASGI** | Uvicorn 0.30+ | Servidor ASGI para ejecución en producción y desarrollo |
| **ORM** | SQLAlchemy 2.0 | Mapeo objeto-relacional agnóstico (SQLite / PostgreSQL) |
| **Seguridad & JWT** | python-jose & passlib[bcrypt] | Emisión de tokens firmados HS256 y hashing seguro de contraseñas |
| **Configuración** | pydantic-settings | Inyección tipada de variables de entorno desde `.env` |
| **Contenedores** | Docker & Docker Compose | Contenedores para la API y PostgreSQL |

---

## 2. Estructura del Proyecto

```text
Api-moviles/
├── app/
│   ├── __init__.py
│   ├── config.py             # Configuración con BaseSettings (variables de entorno)
│   ├── database.py           # Conexión SQLAlchemy, SessionLocal y get_db()
│   ├── security.py           # Hashing bcrypt, JWT y dependencia get_current_user
│   ├── models/
│   │   ├── __init__.py
│   │   ├── user.py           # Modelo ORM User
│   │   └── task.py           # Modelo ORM Task (5+ atributos propios)
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── user.py           # Schemas Pydantic de Usuario y Token
│   │   └── task.py           # Schemas Pydantic de Tareas y Validaciones
│   ├── routers/
│   │   ├── __init__.py
│   │   ├── auth.py           # Endpoints /auth/register y /auth/login
│   │   └── tasks.py          # Endpoints CRUD /tareas
│   └── main.py               # Instancia FastAPI, CORS, Lifespan y Health Check
├── .dockerignore
├── .env
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── requirements.txt
├── run.sh                    # Script ejecutable para iniciar la API en local
└── README.md
```

---

## 3. Modelos de Base de Datos (ORM)

### 3.1 Modelo `User` (Tabla `users`)
Representa la cuenta de un usuario en el sistema.

| Columna | Tipo SQLAlchemy | Restricciones | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `Integer` | Primary Key, Index | Identificador único del usuario |
| `name` | `String(100)` | Nullable | Nombre completo del usuario |
| `email` | `String(255)` | Unique, Index, Not Null | Correo electrónico institucional o personal |
| `hashed_password` | `String(255)` | Not Null | Hash de la contraseña generado con bcrypt |
| `created_at` | `DateTime(timezone=True)` | Not Null, Server Default | Fecha y hora UTC de registro |
| `tasks` | `relationship` | Cascade="all, delete-orphan" | Colección de tareas asociadas al usuario |

---

### 3.2 Modelo `Task` (Tabla `tasks`)
Recurso principal del dominio con identificador y 5 atributos propios (cumpliendo los criterios de la rúbrica):

| Columna | Tipo SQLAlchemy | Restricciones | Tipo de Atributo | Descripción |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `Integer` | Primary Key, Index | Identificador | ID numérico autoincremental |
| `title` | `String(255)` | Not Null | Texto | Título descriptivo de la tarea |
| `description` | `Text` | Nullable | Texto | Descripción detallada de la tarea |
| `priority` | `Integer` | Not Null, Default=2 | Numérico | Nivel de prioridad: 1 (Baja), 2 (Media), 3 (Alta) |
| `completed` | `Boolean` | Not Null, Default=False | Booleano | Estado de completado de la tarea |
| `due_date` | `String(50)` | Nullable | Fecha | Fecha límite en formato `AAAA-MM-DD` |
| `user_id` | `Integer` | ForeignKey(`users.id`), Index | Foránea | ID del usuario propietario |
| `created_at` | `DateTime(timezone=True)` | Not Null, Server Default | Fecha | Fecha y hora UTC de creación |

---

## 4. Esquemas de Datos (Pydantic v2)

### 4.1 Esquemas de Usuario
- **`UserCreate`**:
  - `name`: `Optional[str]` (máx. 100 caracteres).
  - `email`: `EmailStr` (validación de formato de correo).
  - `password`: `str` (longitud mínima de 8 caracteres).
- **`UserResponse`**:
  - `id`: `int`
  - `name`: `Optional[str]`
  - `email`: `EmailStr`
  - `created_at`: `datetime`
- **`Token`**:
  - `access_token`: `str`
  - `token_type`: `str` (`"bearer"`)
  - `user`: `Optional[UserResponse]`

### 4.2 Esquemas de Tarea
- **`TaskCreate`**:
  - `title`: `str` (obligatorio, longitud entre 1 y 255 caracteres).
  - `description`: `Optional[str]` (opcional).
  - `priority`: `int` (rango entre 1 y 3, por defecto 2).
  - `completed`: `bool` (por defecto `False`).
  - `due_date`: `Optional[str]` (fecha en formato `AAAA-MM-DD`).
- **`TaskUpdate`**:
  - Todos los campos son opcionales para permitir actualización parcial:
    - `title`: `Optional[str]`
    - `description`: `Optional[str]`
    - `priority`: `Optional[int]` (rango 1..3)
    - `completed`: `Optional[bool]`
    - `due_date`: `Optional[str]`
- **`TaskResponse`**:
  - Incluye todos los atributos de `TaskCreate` más:
    - `id`: `int`
    - `user_id`: `int`
    - `created_at`: `datetime`

---

## 5. Contratos de la API (Endpoints)

### 5.1 Health Check

#### `GET /`
Verifica el estado del servicio.
- **Autenticación**: No requerida.
- **Respuesta 200 OK**:
```json
{
  "status": "online",
  "service": "To-Do List API",
  "docs": "/docs",
  "version": "1.0.0"
}
```

---

### 5.2 Autenticación

#### `POST /auth/register` (o alias `/register`)
Registra un nuevo usuario en la plataforma.
- **Autenticación**: No requerida.
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "name": "Edgar Solis",
  "email": "usuario@ejemplo.com",
  "password": "Password123"
}
```
- **Respuesta 201 Created**:
```json
{
  "id": 1,
  "name": "Edgar Solis",
  "email": "usuario@ejemplo.com",
  "created_at": "2026-11-08T19:35:51"
}
```
- **Errores posibles**:
  - `400 Bad Request`: Si el correo ya está registrado (`"El correo electrónico ya se encuentra registrado."`).
  - `422 Unprocessable Entity`: Si el correo no tiene formato válido o la contraseña tiene menos de 8 caracteres.

---

#### `POST /auth/login` (o alias `/login`)
Inicia sesión mediante OAuth2 Password Flow y devuelve el token JWT Bearer.
- **Autenticación**: No requerida.
- **Content-Type**: `application/x-www-form-urlencoded`
- **Form Data**:
  - `username`: `usuario@ejemplo.com` (correo electrónico)
  - `password`: `Password123`
- **Respuesta 200 OK**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "user": {
    "id": 1,
    "name": "Edgar Solis",
    "email": "usuario@ejemplo.com",
    "created_at": "2026-11-08T19:35:51"
  }
}
```
- **Errores posibles**:
  - `401 Unauthorized`: Correo electrónico o contraseña incorrectos.

---

### 5.3 Tareas (CRUD Principal)

> **Importante**: Todos los endpoints de `/tareas` requieren la cabecera:  
> `Authorization: Bearer <access_token>`

#### `GET /tareas` (Listar Tareas)
Obtiene la lista de tareas pertenecientes exclusivamente al usuario autenticado.
- **Query Parameters**:
  - `completed` *(opcional, booleano)*: `true` para completadas, `false` para pendientes. Si se omite, retorna todas.
  - `skip` *(opcional, entero, def: 0)*: Desplazamiento para paginación.
  - `limit` *(opcional, entero, def: 50, máx: 100)*: Límite de registros.
- **Respuesta 200 OK**:
```json
[
  {
    "id": 1,
    "title": "Configurar notificaciones push",
    "description": "Integrar servicio en la app móvil",
    "priority": 3,
    "completed": false,
    "due_date": "2026-11-20",
    "user_id": 1,
    "created_at": "2026-11-08T19:36:05"
  }
]
```

---

#### `POST /tareas` (Crear Tarea)
Crea una nueva tarea asignada automáticamente al usuario del token.
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "title": "Preparar presentación del proyecto",
  "description": "Elaborar diapositivas para la Unidad 3",
  "priority": 2,
  "completed": false,
  "due_date": "2026-11-15"
}
```
- **Respuesta 201 Created**:
```json
{
  "id": 2,
  "title": "Preparar presentación del proyecto",
  "description": "Elaborar diapositivas para la Unidad 3",
  "priority": 2,
  "completed": false,
  "due_date": "2026-11-15",
  "user_id": 1,
  "created_at": "2026-11-08T19:40:00"
}
```

---

#### `GET /tareas/{id}` (Ver Detalle de Tarea)
Obtiene los detalles de una tarea verificando que pertenezca al usuario del token.
- **Path Parameter**:
  - `id` *(entero)*: ID de la tarea.
- **Respuesta 200 OK**:
```json
{
  "id": 1,
  "title": "Configurar notificaciones push",
  "description": "Integrar servicio en la app móvil",
  "priority": 3,
  "completed": false,
  "due_date": "2026-11-20",
  "user_id": 1,
  "created_at": "2026-11-08T19:36:05"
}
```
- **Errores posibles**:
  - `404 Not Found`: Si la tarea no existe o pertenece a otro usuario.

---

#### `PUT /tareas/{id}` (Actualizar Tarea)
Actualiza uno o varios atributos de una tarea propia.
- **Content-Type**: `application/json`
- **Request Body (ejemplo de marcar como completada)**:
```json
{
  "completed": true
}
```
- **Request Body (ejemplo de edición completa)**:
```json
{
  "title": "Configurar notificaciones push y Analytics",
  "priority": 3,
  "due_date": "2026-11-25",
  "completed": false
}
```
- **Respuesta 200 OK**:
```json
{
  "id": 1,
  "title": "Configurar notificaciones push y Analytics",
  "description": "Integrar servicio en la app móvil",
  "priority": 3,
  "completed": false,
  "due_date": "2026-11-25",
  "user_id": 1,
  "created_at": "2026-11-08T19:36:05"
}
```
- **Errores posibles**:
  - `404 Not Found`: Si la tarea no existe o no pertenece al usuario.

---

#### `DELETE /tareas/{id}` (Eliminar Tarea)
Elimina de forma permanente una tarea perteneciente al usuario autenticado.
- **Path Parameter**:
  - `id` *(entero)*: ID de la tarea a eliminar.
- **Respuesta 204 No Content**: *(Cuerpo vacío)*
- **Errores posibles**:
  - `404 Not Found`: Si la tarea no existe o pertenece a otro usuario.

---

## 6. Manejo de Errores y Códigos de Estado

La API sigue las convenciones RFC de HTTP REST:

| Código | Significado | Causa común | Formato de Respuesta |
| :---: | :--- | :--- | :--- |
| **200** | OK | Petición exitosa con datos de retorno | Objeto JSON o Lista |
| **201** | Created | Recurso creado exitosamente (Usuario o Tarea) | Objeto JSON creado |
| **204** | No Content | Eliminación exitosa sin cuerpo de retorno | *(Vacío)* |
| **400** | Bad Request | Conflicto de negocio (ej: correo duplicado) | `{"detail": "Mensaje explicativo"}` |
| **401** | Unauthorized | Token ausente, expirado o credenciales erróneas | `{"detail": "Credenciales inválidas..."}` |
| **404** | Not Found | Recurso inexistente o ajeno al usuario | `{"detail": "La tarea con ID X no fue encontrada..."}` |
| **422** | Unprocessable Entity | Error de validación en esquema Pydantic | `{"detail": [{"loc": [...], "msg": "..."}]}` |
| **500** | Internal Server Error | Error no controlado en el servidor | `{"detail": "Error interno del servidor"}` |

---

## 7. Variables de Entorno

El archivo `.env` se configura a partir de `.env.example`:

```env
# Entorno
ENVIRONMENT=development
PROJECT_NAME="To-Do List API"
DEBUG=True

# Seguridad JWT (Generar con: openssl rand -hex 32)
SECRET_KEY=cambiar_esta_clave_por_una_cadena_aleatoria_super_segura_de_minimo_32_bytes
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=60

# Base de Datos (SQLite local por defecto)
DATABASE_URL=sqlite:///./todos.db

# CORS (Permitir peticiones de clientes móviles y navegadores)
ALLOWED_ORIGINS=["*"]
```

---

## 8. Instalación y Ejecución Local

### Ejecución Directa con el Script Automático (Recomendado)
Funciona en cualquier shell (**Bash, Zsh, Fish Shell**):

```bash
cd /home/edgar/Descargas/Api-moviles
./run.sh
```

### Ejecución Manual según tu Shell

#### En Fish Shell:
```fish
cd /home/edgar/Descargas/Api-moviles
source venv/bin/activate.fish
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

#### En Bash / Zsh:
```bash
cd /home/edgar/Descargas/Api-moviles
source venv/bin/activate
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## 9. Consumo desde Clientes Móviles

### Direcciones de Red según el Entorno:
- **Emulador de Android Studio**: Conectar a `http://10.0.2.2:8000/`. Android redirige automáticamente esa IP a la máquina host.
- **Dispositivo Físico (vía Wi-Fi)**: Conectar a la IP local de tu computadora (ej: `http://192.168.1.75:8000/`).
- **Servidor en la Nube (AWS EC2)**:
  - **HTTPS Seguro (Recomendado)**: `https://18.204.112.209.sslip.io/` o `https://18.204.112.209.nip.io/` (con certificado SSL oficial de Let's Encrypt y TLS 1.3).
  - **HTTP Estándar**: `http://18.204.112.209/`.

---

## 10. Despliegue en AWS EC2

### Resumen del Flujo de Producción:
1. **Instancia**: Ubuntu 24.04 LTS en AWS EC2 (`18.204.112.209`) con puertos 22 (SSH), 80 (HTTP) y 443 (HTTPS) abiertos en el *Security Group*.
2. **Servicio Systemd (`/etc/systemd/system/fastapi-todo.service`)**:
   Mantiene el proceso Uvicorn activo en segundo plano y con reinicio automático:
   ```ini
   [Unit]
   Description=FastAPI To-Do Application Uvicorn Service
   After=network.target

   [Service]
   User=ubuntu
   WorkingDirectory=/var/www/api-moviles
   ExecStart=/var/www/api-moviles/venv/bin/uvicorn app.main:app --workers 3 --host 127.0.0.1 --port 8000
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```
3. **Proxy Inverso Nginx con Cifrado HTTPS (TLS 1.3 / AES-256-GCM)**:
   Nginx escucha en los puertos 80 y 443, redirigiendo el tráfico seguro hacia `127.0.0.1:8000` con certificados SSL emitidos por Let's Encrypt y renovación automática activa mediante `certbot.timer`.
