from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field, ConfigDict


class TaskBase(BaseModel):
    title: str = Field(
        ...,
        min_length=1,
        max_length=255,
        description="Título de la tarea.",
        examples=["Comprar víveres"]
    )
    description: Optional[str] = Field(
        default=None,
        description="Descripción detallada de la tarea.",
        examples=["Comprar leche, huevos y pan"]
    )
    priority: int = Field(
        default=2,
        ge=1,
        le=3,
        description="Nivel de prioridad: 1 = Baja, 2 = Media, 3 = Alta."
    )
    due_date: Optional[str] = Field(
        default=None,
        description="Fecha límite para completar la tarea (formato YYYY-MM-DD)."
    )


class TaskCreate(TaskBase):
    completed: bool = Field(
        default=False,
        description="Estado inicial de la tarea (por defecto False)."
    )


class TaskUpdate(BaseModel):
    title: Optional[str] = Field(
        default=None,
        min_length=1,
        max_length=255,
        description="Nuevo título para la tarea."
    )
    description: Optional[str] = Field(
        default=None,
        description="Nueva descripción de la tarea."
    )
    priority: Optional[int] = Field(
        default=None,
        ge=1,
        le=3,
        description="Nuevo nivel de prioridad (1-3)."
    )
    due_date: Optional[str] = Field(
        default=None,
        description="Nueva fecha límite."
    )
    completed: Optional[bool] = Field(
        default=None,
        description="Nuevo estado de completado."
    )


class TaskResponse(TaskBase):
    id: int
    completed: bool
    user_id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
