--liquibase formatted sql
--changeset init:create-request-table
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS request (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_id UUID,
    url TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_request_id ON request (request_id);
