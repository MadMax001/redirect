--liquibase formatted sql
--changeset init:003_drop_constraint_request

DROP INDEX IF EXISTS idx_unique_request_id;
CREATE INDEX IF NOT EXISTS idx_gpb_id ON request (gpb_id);