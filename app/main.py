from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import engine, Base
import app.models  # Importa los modelos para que Base los reconozca
from app.routers import auth_router, tasks_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Ciclo de vida de la aplicación.

    Crea las tablas en la base de datos al iniciar si no existen.
    """
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(
    title=settings.PROJECT_NAME,
    description=(
        "API REST segura para gestión de tareas multiusuario con autenticación JWT. "
        "Optimizada para aplicaciones móviles (Android/iOS) y aplicaciones web modernas."
    ),
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

# Configuración de CORS para clientes Web y Móviles
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Registrar Routers
app.include_router(auth_router)
app.include_router(tasks_router)


@app.get(
    "/",
    tags=["General"],
    summary="Verificación del servicio (Health Check)"
)
def root():
    return {
        "status": "online",
        "service": settings.PROJECT_NAME,
        "docs": "/docs",
        "version": "1.0.0"
    }
