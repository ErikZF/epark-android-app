-- =============================================================
-- E-PARK · PostgreSQL Schema
-- Migration: 009_email_verification
-- Adds email verification (requirement 6.2): drivers must activate
-- their account via an emailed link before they can sign in.
-- =============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verified     BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS verification_token VARCHAR(64),
    ADD COLUMN IF NOT EXISTS verification_sent_at TIMESTAMPTZ;

-- Existing accounts (seeded admin + test drivers) are treated as already
-- verified so they keep working; only new registrations start unverified.
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE;

CREATE INDEX IF NOT EXISTS idx_users_verification_token ON users(verification_token);
