-- =============================================================================
-- Migration V1: Initial Schemas and Extensions
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Isolated Bounded Context Schemas
CREATE SCHEMA IF NOT EXISTS identity_schema;
CREATE SCHEMA IF NOT EXISTS finance_schema;
CREATE SCHEMA IF NOT EXISTS amenities_schema;
CREATE SCHEMA IF NOT EXISTS parking_schema;

-- =============================================================================
-- IDENTITY_SCHEMA: Tables
-- =============================================================================

-- Table: houses
CREATE TABLE IF NOT EXISTS identity_schema.houses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_number VARCHAR(20) NOT NULL,
    street_or_block VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_houses_house_number UNIQUE (house_number)
);

-- Table: users
CREATE TABLE IF NOT EXISTS identity_schema.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_id UUID REFERENCES identity_schema.houses(id) ON DELETE SET NULL,
    id_type VARCHAR(20) NOT NULL,
    id_number VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    residential_role VARCHAR(30) NOT NULL,
    board_role VARCHAR(30) NOT NULL DEFAULT 'NONE',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_id_number UNIQUE (id_number)
);

-- Invariant Enforcement: Only 1 active OWNER per house
CREATE UNIQUE INDEX IF NOT EXISTS uq_house_single_owner 
    ON identity_schema.users(house_id) 
    WHERE residential_role = 'OWNER';

-- Invariant Enforcement: Only 1 active TENANT per house
CREATE UNIQUE INDEX IF NOT EXISTS uq_house_single_tenant 
    ON identity_schema.users(house_id) 
    WHERE residential_role = 'TENANT';

-- Table: invitation_tokens
CREATE TABLE IF NOT EXISTS identity_schema.invitation_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES identity_schema.users(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_invitation_tokens_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_invitation_tokens_user_id ON identity_schema.invitation_tokens(user_id);
