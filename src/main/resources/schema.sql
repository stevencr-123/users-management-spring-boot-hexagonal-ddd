-- Tipos ENUM en PostgreSQL
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('ADMIN', 'MEMBER', 'REVIEWER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_status') THEN
        CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING', 'BLOCKED');
    END IF;
END $$;

-- Creación de la tabla
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role NOT NULL,
    status      user_status NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inserción inicial
INSERT INTO users (id, name, email, password, role, status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'admin@example.com',
    '$2a$12$placeholderHashReplaceWithRealBCryptHash',
    'ADMIN',
    'ACTIVE'
) ON CONFLICT (id) DO NOTHING;