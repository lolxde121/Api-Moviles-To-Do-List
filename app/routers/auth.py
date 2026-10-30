from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.config import settings
from app.database import get_db
from app.models.user import User
from app.schemas.user import UserCreate, UserResponse, Token
from app.security import (
    get_password_hash,
    verify_password,
    create_access_token,
)

router = APIRouter(
    prefix="/auth",
    tags=["Autenticación"]
)


@router.post(
    "/register",
    response_model=UserResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Registrar nuevo usuario",
    description="Crea una nueva cuenta de usuario validando que el email no esté registrado previamente."
)
def register(user_in: UserCreate, db: Session = Depends(get_db)):
    # 1. Verificar si el correo ya existe
    existing_user = db.query(User).filter(User.email == user_in.email).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El correo electrónico ya se encuentra registrado."
        )

    # 2. Hashear la contraseña de forma segura
    hashed_pwd = get_password_hash(user_in.password)

    # 3. Crear el nuevo usuario en base de datos
    new_user = User(
        email=user_in.email,
        hashed_password=hashed_pwd
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    return new_user


@router.post(
    "/login",
    response_model=Token,
    summary="Iniciar sesión y obtener token JWT",
    description="Autentica credenciales (email y password) utilizando OAuth2 Password Flow y retorna un token JWT Bearer."
)
def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    # En OAuth2PasswordRequestForm, el campo 'username' contiene el email del usuario
    user = db.query(User).filter(User.email == form_data.username).first()

    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo electrónico o contraseña incorrectos.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Crear token con expiración configurada
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id), "email": user.email},
        expires_delta=access_token_expires
    )

    return {
        "access_token": access_token,
        "token_type": "bearer"
    }
