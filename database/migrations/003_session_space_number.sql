-- =============================================================
-- E-PARK · Add physical space number to sessions
-- Migration: 003_session_space_number
-- Run AFTER 002_seed_data.sql
--
-- A session now records which physical parking spot (espacio) the
-- driver took inside a zone, so the same spot cannot be double-booked
-- while a session is active.
-- =============================================================

-- DEFAULT 1 is a valid placeholder (satisfies the CHECK) for any pre-existing
-- rows; the default is dropped immediately so new sessions must supply a real
-- space number.
ALTER TABLE sessions
    ADD COLUMN space_number INT NOT NULL DEFAULT 1
        CHECK (space_number > 0);

ALTER TABLE sessions
    ALTER COLUMN space_number DROP DEFAULT;

-- A physical spot can only host one active session at a time.
CREATE UNIQUE INDEX idx_sessions_active_space
    ON sessions(zone_id, space_number)
    WHERE status = 'active';
