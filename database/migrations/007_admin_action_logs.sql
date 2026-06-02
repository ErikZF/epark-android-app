-- =============================================================
-- E-PARK · PostgreSQL Schema
-- Migration: 007_admin_action_logs
-- Adds an immutable audit trail of privileged admin actions so
-- admin power can't be abused without leaving a record.
-- (Already present in 001 for fresh installs; this brings existing
--  databases up to date.)
-- =============================================================

CREATE TABLE IF NOT EXISTS admin_action_logs (
    id         BIGSERIAL    PRIMARY KEY,
    admin_id   INT          NOT NULL REFERENCES users(id),
    action     VARCHAR(100) NOT NULL,   -- e.g. 'zone.create', 'zone.update', 'report.view'
    details    TEXT,                    -- human-readable summary of what changed
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_action_logs_admin   ON admin_action_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_admin_action_logs_created ON admin_action_logs(created_at);
