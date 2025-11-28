--liquibase formatted sql
--changeset init:modify-request-table-add_2_columns

ALTER TABLE IF EXISTS request
    ADD COLUMN IF NOT EXISTS client_ip TEXT,
    ADD COLUMN IF NOT EXISTS client_agent TEXT;
ALTER TABLE IF EXISTS request
    RENAME COLUMN request_id TO gpb_id;
