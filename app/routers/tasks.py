from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status, Response
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.task import Task
from app.models.user import User
from app.schemas.task import TaskCreate, TaskResponse, TaskUpdate
from app.security import get_current_user

router = APIRouter(
    prefix="/tareas",
    tags=["Tareas"]
)


@router.get(
    "",
    response_model=List[TaskResponse],
    summary="Listar tareas del usuario",
    description="Retorna las tareas pertenecientes exclusivamente al usuario autenticado, con soporte para filtrado por estado de completado y paginación."
)
def list_tasks(
    completed: Optional[bool] = Query(
        default=None,
        description="Filtrar por estado completado (true o false). Si se omite, retorna todas."
    ),
    skip: int = Query(
        default=0,
        ge=0,
        description="Número de registros a omitir (desplazamiento para paginación)."
    ),
    limit: int = Query(
        default=50,
        ge=1,
        le=100,
        description="Número máximo de tareas a retornar (máximo 100)."
    ),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    query = db.query(Task).filter(Task.user_id == current_user.id)

    # Filtrado opcional por estado
    if completed is not None:
        query = query.filter(Task.completed == completed)

    # Ordenar por fecha de creación descendente y aplicar paginación
    tasks = query.order_by(Task.created_at.desc()).offset(skip).limit(limit).all()
    return tasks


@router.post(
    "",
    response_model=TaskResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Crear nueva tarea",
    description="Crea una nueva tarea asignada automáticamente al usuario autenticado."
)
def create_task(
    task_in: TaskCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    new_task = Task(
        title=task_in.title,
        description=task_in.description,
        priority=task_in.priority,
        due_date=task_in.due_date,
        completed=task_in.completed,
        user_id=current_user.id
    )
    db.add(new_task)
    db.commit()
    db.refresh(new_task)
    return new_task


@router.get(
    "/{id}",
    response_model=TaskResponse,
    summary="Obtener detalle de una tarea",
    description="Retorna los datos de una tarea específica siempre y cuando pertenezca al usuario autenticado."
)
def get_task(
    id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    task = db.query(Task).filter(Task.id == id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"La tarea con ID {id} no fue encontrada o no tiene permisos para acceder a ella."
        )
    return task


@router.put(
    "/{id}",
    response_model=TaskResponse,
    summary="Actualizar una tarea",
    description="Actualiza el título, descripción, prioridad, fecha límite o estado de completado de una tarea propia."
)
def update_task(
    id: int,
    task_in: TaskUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    task = db.query(Task).filter(Task.id == id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"La tarea con ID {id} no fue encontrada o no tiene permisos para modificarla."
        )

    # Actualizar solo los campos proporcionados en la petición
    update_data = task_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(task, field, value)

    db.commit()
    db.refresh(task)
    return task


@router.delete(
    "/{id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Eliminar una tarea",
    description="Elimina de forma permanente una tarea perteneciente al usuario autenticado."
)
def delete_task(
    id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    task = db.query(Task).filter(Task.id == id, Task.user_id == current_user.id).first()
    if not task:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"La tarea con ID {id} no fue encontrada o no tiene permisos para eliminarla."
        )

    db.delete(task)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
