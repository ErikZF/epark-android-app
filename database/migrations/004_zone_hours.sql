-- =============================================================
-- E-PARK · PostgreSQL Schema
-- Migration: 004_zone_hours
-- Adds operating hours to the zones table.
-- open_hour  : local (Costa Rica, UTC-6) hour (0-23) when the zone opens.
-- close_hour : local (Costa Rica, UTC-6) hour (1-24) when the zone closes; 24 = midnight of next day.
-- =============================================================

ALTER TABLE zones
    ADD COLUMN IF NOT EXISTS open_hour  SMALLINT NOT NULL DEFAULT 6
        CHECK (open_hour  BETWEEN 0 AND 23),
    ADD COLUMN IF NOT EXISTS close_hour SMALLINT NOT NULL DEFAULT 22
        CHECK (close_hour BETWEEN 1 AND 24);
