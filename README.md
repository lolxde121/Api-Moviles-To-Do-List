# To-Do List API Multi-usuario (FastAPI + JWT + PostgreSQL / SQLite)

API REST completa, modular y escalable para una aplicación de Lista de Tareas (*To-Do List*) multiusuario construida con **FastAPI**, **Pydantic v2**, **SQLAlchemy** y autenticación segura **OAuth2 con JWT**.

Diseñada para ser consumida directamente desde aplicaciones móviles (**Android / iOS**) y aplicaciones web (**React, Vue, Flutter, etc.**).

---

## 1. Arquitectura y Tecnologías

- **Framework Web**: [FastAPI](https://fastapi.tiangolo.com/) (Pydantic v2 para validación estricta de esquemas).
- **Servidor ASGI**: [Uvicorn](https://www.uvicorn.org/).
- **ORM**: [SQLAlchemy 2.0](https://www.sqlalchemy.org/) con soporte agnóstico para **SQLite** (desarrollo/pruebas) y **PostgreSQL** (producción).
- **Autenticación**: OAuth2 Password Flow con tokens **JWT** (`python-jose`) y hasheo seguro de contraseñas con `passlib[bcrypt]`.
- **Configuración**: `pydantic-settings` con carga de variables desde archivos `.env`.
- **Contenedores**: `Dockerfile` optimizado y `docker-compose.yml` para orquestación con PostgreSQL.
- **CORS**: Habilitado y configurable para permitir peticiones cross-origin de navegadores y apps móviles.

---

## 2. Estructura del Proyecto

```text
Api-moviles/
├── app/
│   ├── __init__.py
│   ├── config.py             # Configuración centralizada (BaseSettings)
│   ├── database.py           # Conexión SQLAlchemy, SessionLocal y get_db()
│   ├── security.py           # Hashing bcrypt, JWT y dependencia get_current_user
│   ├── models/
│   │   ├── __init__.py
│   │   ├── user.py           # Modelo User (id, email, hashed_password, created_at)
│   │   └── task.py           # Modelo Task (id, title, description, completed, user_id)
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── user.py           # Esquemas Pydantic v2 de Usuario y Token
│   │   └── task.py           # Esquemas Pydantic v2 de Tareas y Paginación
│   ├── routers/
│   │   ├── __init__.py
│   │   ├── auth.py           # Endpoints /auth/register y /auth/login
│   │   └── tasks.py          # Endpoints CRUD /tareas
│   └── main.py               # Inicialización FastAPI, CORS y ciclo de vida
├── .dockerignore
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── requirements.txt
└── README.md
```

---

## 3. Instalación y Ejecución Local

### Prerrequisitos
- Python 3.10 o superior instalado.
- Administrador de paquetes `pip`.

### Paso 1: Clonar el repositorio y navegar a la carpeta
```bash
git clone <URL_REPOSITORIO>
cd Api-moviles
```

### Paso 2: Crear y activar un entorno virtual
```bash
python3 -m venv venv

# En Linux/macOS:
source venv/bin/activate

# En Windows:
# venv\Scripts\activate
```

### Paso 3: Instalar dependencias
```bash
pip install --upgrade pip
pip install -r requirements.txt
```

### Paso 4: Configurar variables de entorno
Crea tu archivo `.env` a partir de la plantilla:
```bash
cp .env.example .env
```
Genera una clave secreta para `SECRET_KEY`:
```bash
openssl rand -hex 32
```
Edita `.env` y asigna el valor generado a `SECRET_KEY`.

### Paso 5: Ejecutar el servidor de desarrollo
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```
La API estará disponible en: `http://localhost:8000`  
Documentación interactiva Swagger UI: `http://localhost:8000/docs`  
Documentación ReDoc: `http://localhost:8000/redoc`

---

## 4. Ejecución con Docker y Docker Compose

Para levantar la API junto a una base de datos PostgreSQL en contenedores:

```bash
docker compose up -d --build
```

- **API REST**: `http://localhost:8000`
- **PostgreSQL**: `localhost:5432`
- **Para ver logs**: `docker compose logs -f api`
- **Para detener los servicios**: `docker compose down`

---

## 5. Endpoints y Ejemplos de Consumo

### 5.1 Registro de Usuario
- **Ruta**: `POST /auth/register`
- **Body (JSON)**:
```json
{
  "email": "dev@example.com",
  "password": "Password123!"
}
```
- **Respuesta (201 Created)**:
```json
{
  "email": "dev@example.com",
  "id": 1,
  "created_at": "2026-10-30T15:30:00Z"
}
```

### 5.2 Inicio de Sesión (Login)
- **Ruta**: `POST /auth/login`
- **Content-Type**: `application/x-www-form-urlencoded`
- **Form Data**:
  - `username`: `dev@example.com`
  - `password`: `Password123!`
- **Respuesta (200 OK)**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsIn...",
  "token_type": "bearer"
}
```

> **Nota para clientes móviles / web**: Guardar `access_token` en almacenamiento seguro (Keychain/Keystore en móviles o `HttpOnly cookie`/`Secure Storage` en web) y enviarlo en las siguientes peticiones en la cabecera:
> `Authorization: Bearer <access_token>`

### 5.3 Crear Tarea
- **Ruta**: `POST /tareas`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Body (JSON)**:
```json
{
  "title": "Configurar notificaciones push en Android",
  "description": "Integrar Firebase Cloud Messaging",
  "completed": false
}
```
- **Respuesta (201 Created)**:
```json
{
  "id": 1,
  "title": "Configurar notificaciones push en Android",
  "description": "Integrar Firebase Cloud Messaging",
  "completed": false,
  "user_id": 1,
  "created_at": "2026-10-30T15:35:00Z"
}
```

### 5.4 Listar Tareas con Filtro y Paginación
- **Ruta**: `GET /tareas?completed=false&skip=0&limit=10`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Respuesta (200 OK)**:
```json
[
  {
    "id": 1,
    "title": "Configurar notificaciones push en Android",
    "description": "Integrar Firebase Cloud Messaging",
    "completed": false,
    "user_id": 1,
    "created_at": "2026-10-30T15:35:00Z"
  }
]
```

### 5.5 Obtener Detalle de Tarea
- **Ruta**: `GET /tareas/1`
- **Headers**: `Authorization: Bearer <TOKEN>`

### 5.6 Actualizar Tarea
- **Ruta**: `PUT /tareas/1`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Body (JSON)**:
```json
{
  "completed": true
}
```

### 5.7 Eliminar Tarea
- **Ruta**: `DELETE /tareas/1`
- **Headers**: `Authorization: Bearer <TOKEN>`
- **Respuesta (204 No Content)**

---

## 6. Guía de Despliegue en AWS EC2 (Ubuntu + Nginx + Systemd)

Esta guía describe cómo desplegar la API en una instancia **AWS EC2 con Ubuntu Server (22.04 / 24.04 LTS)** en modo producción.

### 6.1 Crear la Instancia en AWS EC2
1. Ir a la consola de AWS -> **EC2** -> **Launch Instance**.
2. **AMI**: Ubuntu Server 22.04 LTS o 24.04 LTS (64-bit x86).
3. **Tipo de Instancia**: `t3.micro` o `t3.small` (apta para capa gratuita).
4. **Key Pair**: Crear o seleccionar tu par de claves SSH (`.pem`).
5. **Security Group (Reglas de Entrada / Inbound Rules)**:
   - SSH (Puerto 22) -> Tu IP o `0.0.0.0/0`.
   - HTTP (Puerto 80) -> `0.0.0.0/0`.
   - HTTPS (Puerto 443) -> `0.0.0.0/0`.
6. Lanzar la instancia y conectar por SSH:
   ```bash
   ssh -i "tu-llave.pem" ubuntu@<IP-PUBLICA-EC2>
   ```

---

### 6.2 Actualizar el Servidor e Instalar Dependencias
Dentro de la instancia EC2:
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y python3-pip python3-venv git nginx
```

---

### 6.3 Clonar y Configurar el Proyecto
```bash
# Navegar a /var/www
cd /var/www
sudo git clone <URL_DE_TU_REPOSITORIO> api-moviles
sudo chown -R ubuntu:ubuntu /var/www/api-moviles
cd /var/www/api-moviles

# Crear el entorno virtual
python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

# Configurar el archivo .env para producción
cp .env.example .env
nano .env
```
> Asegúrate de colocar `DEBUG=False`, un `SECRET_KEY` generado con `openssl rand -hex 32` y la URL de tu base de datos (PostgreSQL en AWS RDS o SQLite local).

---

### 6.4 Configurar el Servicio con Systemd (Gestor de Procesos)
Crea un servicio para que Uvicorn se ejecute en segundo plano y se reinicie automáticamente ante fallos o reinicios de la máquina:

```bash
sudo nano /etc/systemd/system/fastapi-todo.service
```

Pega el siguiente contenido:
```ini
[Unit]
Description=FastAPI To-Do Application Uvicorn Service
After=network.target

[Service]
User=ubuntu
Group=www-data
WorkingDirectory=/var/www/api-moviles
Environment="PATH=/var/www/api-moviles/venv/bin"
ExecStart=/var/www/api-moviles/venv/bin/uvicorn app.main:app --workers 3 --host 127.0.0.1 --port 8000
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Habilitar e iniciar el servicio:
```bash
sudo systemctl daemon-reload
sudo systemctl start fastapi-todo
sudo systemctl enable fastapi-todo
sudo systemctl status fastapi-todo
```

---

### 6.5 Configurar Nginx como Reverse Proxy
Crea el archivo de configuración para Nginx:

```bash
sudo nano /etc/nginx/sites-available/fastapi-todo
```

Pega la siguiente configuración (reemplaza `api.tudominio.com` o la IP de tu EC2):
```nginx
server {
    listen 80;
    server_name api.tudominio.com; # O coloca la IP pública si no tienes dominio

    client_max_body_size 10M;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

Habilitar el sitio y reiniciar Nginx:
```bash
sudo ln -s /etc/nginx/sites-available/fastapi-todo /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

---

### 6.6 Habilitar HTTPS Gratuito con Let's Encrypt (Certbot)
*(Recomendado para producción y obligatorio para apps móviles en Android/iOS por políticas de seguridad de red)*

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d api.tudominio.com
```
Certbot configurará automáticamente la renovación periódica de certificados SSL.

---

### 6.7 Comandos de Monitoreo y Mantenimiento
- **Ver logs de la API en tiempo real**:
  ```bash
  journalctl -u fastapi-todo -f
  ```
- **Ver logs de Nginx**:
  ```bash
  sudo tail -f /var/log/nginx/access.log
  sudo tail -f /var/log/nginx/error.log
  ```
- **Reiniciar la API tras una actualización de código**:
  ```bash
  cd /var/www/api-moviles
  git pull
  source venv/bin/activate && pip install -r requirements.txt
  sudo systemctl restart fastapi-todo
  ```
