-- =============================================================
-- E-PARK · Migration: 008_fine_space_number
-- Adds the spot number recorded on each fine.
-- =============================================================

ALTER TABLE fines
    ADD COLUMN IF NOT EXISTS space_number INT NOT NULL DEFAULT 1 CHECK (space_number > 0);
