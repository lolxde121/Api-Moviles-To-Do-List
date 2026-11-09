#!/usr/bin/env bash
# Script para iniciar la API en local sin importar la shell (bash, zsh, fish, etc.)
cd "$(dirname "$0")"
./venv/bin/uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
