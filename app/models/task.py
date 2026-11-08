from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.database import Base


class Task(Base):
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    priority = Column(Integer, default=2, nullable=False) # 1: Baja, 2: Media, 3: Alta
    completed = Column(Boolean, default=False, nullable=False)
    due_date = Column(String(50), nullable=True) # Formato YYYY-MM-DD
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Relación inversa con el Usuario propietario
    owner = relationship("User", back_populates="tasks")
