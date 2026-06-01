-- =============================================================
-- E-PARK · PostgreSQL Schema
-- Migration: 006_user_national_id
-- Adds national_id (cédula) to users table.
-- =============================================================

ALTER TABLE users
    ADD COLUMN national_id VARCHAR(20) UNIQUE;
