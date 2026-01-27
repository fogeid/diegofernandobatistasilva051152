-- docker/postgres/init.sql
-- Script de inicialização do PostgreSQL para Docker

-- Cria extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Configura timezone
SET timezone = 'America/Cuiaba';

-- Garante que o schema public existe
CREATE SCHEMA IF NOT EXISTS public;

-- Mensagem de sucesso
DO $$
BEGIN
    RAISE NOTICE 'PostgreSQL inicializado com sucesso para musicdb';
END $$;