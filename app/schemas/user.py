from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, Field, ConfigDict


class UserBase(BaseModel):
    name: Optional[str] = Field(default=None, max_length=100, description="Nombre del usuario.")
    email: EmailStr


class UserCreate(UserBase):
    password: str = Field(
        ...,
        min_length=8,
        max_length=128,
        description="Contraseña del usuario con un mínimo de 8 caracteres."
    )


class LoginRequest(BaseModel):
    email: Optional[EmailStr] = Field(
        default=None,
        description="Correo electrónico registrado",
        examples=["usuario@ejemplo.com"]
    )
    username: Optional[str] = Field(
        default=None,
        description="Alias opcional equivalente al correo",
        examples=["usuario@ejemplo.com"]
    )
    password: str = Field(
        ...,
        description="Contraseña del usuario",
        examples=["Password123"]
    )


class UserResponse(UserBase):
    id: int
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: Optional[UserResponse] = None


class TokenPayload(BaseModel):
    sub: Optional[str] = None
