-- =============================================================
-- E-PARK · PostgreSQL Schema
-- =============================================================

-- ─────────────────────────────────────────────
-- 1. ROLES
-- ─────────────────────────────────────────────
CREATE TABLE roles (
    id   SMALLSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE   -- 'admin', 'driver'
);

-- ─────────────────────────────────────────────
-- 2. MUNICIPALITIES
-- ─────────────────────────────────────────────
CREATE TABLE municipalities (
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    country    VARCHAR(100) NOT NULL DEFAULT 'Costa Rica',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────
-- 3. USERS
-- ─────────────────────────────────────────────
CREATE TABLE users (
    id                    SERIAL       PRIMARY KEY,
    role_id               SMALLINT     NOT NULL REFERENCES roles(id),
    municipality_id       INT          REFERENCES municipalities(id),
    full_name             VARCHAR(200) NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    phone                 VARCHAR(20),
    national_id           VARCHAR(20)  UNIQUE,
    avatar_url            TEXT,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token    VARCHAR(64),
    verification_sent_at  TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email              ON users(email);
CREATE INDEX idx_users_role_id            ON users(role_id);
CREATE INDEX idx_users_verification_token ON users(verification_token);

-- ─────────────────────────────────────────────
-- 4. ZONES
-- open_hour / close_hour: local Costa Rica time (UTC-6), 0-23 / 1-24
-- ─────────────────────────────────────────────
CREATE TABLE zones (
    id               SERIAL         PRIMARY KEY,
    municipality_id  INT            NOT NULL REFERENCES municipalities(id),
    name             VARCHAR(150)   NOT NULL,
    description      TEXT,
    latitude         DECIMAL(10, 8) NOT NULL,
    longitude        DECIMAL(11, 8) NOT NULL,
    total_spots      INT            NOT NULL CHECK (total_spots > 0),
    hourly_rate      DECIMAL(10, 2) NOT NULL CHECK (hourly_rate >= 0),
    open_hour        SMALLINT       NOT NULL DEFAULT 6  CHECK (open_hour  BETWEEN 0 AND 23),
    close_hour       SMALLINT       NOT NULL DEFAULT 22 CHECK (close_hour BETWEEN 1 AND 24),
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_zones_municipality ON zones(municipality_id);
CREATE INDEX idx_zones_active       ON zones(is_active);

-- ─────────────────────────────────────────────
-- 5. VEHICLE TYPES
-- ─────────────────────────────────────────────
CREATE TABLE vehicle_types (
    id   SMALLSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE   -- 'car', 'motorcycle', 'truck'
);

-- ─────────────────────────────────────────────
-- 6. VEHICLES
-- ─────────────────────────────────────────────
CREATE TABLE vehicles (
    id              SERIAL      PRIMARY KEY,
    user_id         INT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_type_id SMALLINT    NOT NULL REFERENCES vehicle_types(id),
    plate           VARCHAR(20) NOT NULL UNIQUE,
    nickname        VARCHAR(100),
    brand           VARCHAR(100),
    model           VARCHAR(100),
    color           VARCHAR(50),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_user_id ON vehicles(user_id);
CREATE INDEX idx_vehicles_plate   ON vehicles(plate);

-- ─────────────────────────────────────────────
-- 7. PAYMENT METHODS
-- Card numbers are NEVER stored; only gateway tokens + display metadata.
-- ─────────────────────────────────────────────
CREATE TABLE payment_methods (
    id           SERIAL       PRIMARY KEY,
    user_id      INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_brand   VARCHAR(50),
    last_four    CHAR(4)      NOT NULL,
    expiry_month SMALLINT     NOT NULL CHECK (expiry_month BETWEEN 1 AND 12),
    expiry_year  SMALLINT     NOT NULL,
    token        VARCHAR(255) NOT NULL,
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_methods_user ON payment_methods(user_id);

-- ─────────────────────────────────────────────
-- 8. SESSIONS
-- space_number: the physical parking spot taken inside a zone;
-- the unique partial index prevents double-booking an active spot.
-- ─────────────────────────────────────────────
CREATE TYPE session_status AS ENUM ('active', 'completed', 'cancelled', 'expired');

CREATE TABLE sessions (
    id               SERIAL          PRIMARY KEY,
    user_id          INT             NOT NULL REFERENCES users(id),
    vehicle_id       INT             NOT NULL REFERENCES vehicles(id),
    zone_id          INT             NOT NULL REFERENCES zones(id),
    space_number     INT             NOT NULL CHECK (space_number > 0),
    scheduled_start  TIMESTAMPTZ     NOT NULL,
    scheduled_end    TIMESTAMPTZ     NOT NULL,
    actual_end       TIMESTAMPTZ,
    hourly_rate      DECIMAL(10, 2)  NOT NULL,
    total_cost       DECIMAL(10, 2)  NOT NULL CHECK (total_cost >= 0),
    status           session_status  NOT NULL DEFAULT 'active',
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_session_dates CHECK (scheduled_end > scheduled_start)
);

CREATE INDEX idx_sessions_user_id    ON sessions(user_id);
CREATE INDEX idx_sessions_vehicle_id ON sessions(vehicle_id);
CREATE INDEX idx_sessions_zone_id    ON sessions(zone_id);
CREATE INDEX idx_sessions_status     ON sessions(status);

-- A physical spot can only host one active session at a time.
CREATE UNIQUE INDEX idx_sessions_active_space
    ON sessions(zone_id, space_number)
    WHERE status = 'active';

-- ─────────────────────────────────────────────
-- 9. SESSION EXTENSIONS
-- ─────────────────────────────────────────────
CREATE TABLE session_extensions (
    id              SERIAL         PRIMARY KEY,
    session_id      INT            NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    added_minutes   INT            NOT NULL CHECK (added_minutes > 0),
    additional_cost DECIMAL(10, 2) NOT NULL CHECK (additional_cost >= 0),
    extended_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_session_extensions_session ON session_extensions(session_id);

-- ─────────────────────────────────────────────
-- 10. SESSION RATES
-- Locks the hourly rate applied to a session at creation time.
-- Rate changes on a zone apply only to NEW sessions (requirement 3.2).
-- ─────────────────────────────────────────────
CREATE TABLE session_rates (
    id          SERIAL         PRIMARY KEY,
    session_id  INT            NOT NULL UNIQUE REFERENCES sessions(id) ON DELETE CASCADE,
    hourly_rate DECIMAL(10, 2) NOT NULL CHECK (hourly_rate >= 0),
    locked_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_session_rates_session ON session_rates(session_id);

-- ─────────────────────────────────────────────
-- 11. PAYMENTS
-- reference_type + reference_id is a polymorphic FK (session OR fine).
-- ─────────────────────────────────────────────
CREATE TYPE payment_status    AS ENUM ('pending', 'completed', 'failed', 'refunded');
CREATE TYPE payment_reference AS ENUM ('session', 'fine');

CREATE TABLE payments (
    id                     SERIAL            PRIMARY KEY,
    user_id                INT               NOT NULL REFERENCES users(id),
    payment_method_id      INT               REFERENCES payment_methods(id),
    amount                 DECIMAL(10, 2)    NOT NULL CHECK (amount > 0),
    currency               CHAR(3)           NOT NULL DEFAULT 'CRC',
    status                 payment_status    NOT NULL DEFAULT 'pending',
    reference_type         payment_reference NOT NULL,
    reference_id           INT               NOT NULL,
    gateway_transaction_id VARCHAR(255),
    paid_at                TIMESTAMPTZ,
    created_at             TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_user_id   ON payments(user_id);
CREATE INDEX idx_payments_reference ON payments(reference_type, reference_id);
CREATE INDEX idx_payments_status    ON payments(status);

-- ─────────────────────────────────────────────
-- 12. INVOICES
-- ─────────────────────────────────────────────
CREATE TABLE invoices (
    id             SERIAL         PRIMARY KEY,
    payment_id     INT            NOT NULL UNIQUE REFERENCES payments(id),
    user_id        INT            NOT NULL REFERENCES users(id),
    invoice_number VARCHAR(50)    NOT NULL UNIQUE,
    total_amount   DECIMAL(10, 2) NOT NULL,
    pdf_url        TEXT,
    issued_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_user_id ON invoices(user_id);

-- ─────────────────────────────────────────────
-- 13. FINES
-- ─────────────────────────────────────────────
CREATE TYPE fine_status AS ENUM ('unpaid', 'paid', 'contested', 'dismissed');

CREATE TABLE fines (
    id           SERIAL         PRIMARY KEY,
    issued_by    INT            NOT NULL REFERENCES users(id),
    vehicle_id   INT            NOT NULL REFERENCES vehicles(id),
    zone_id      INT            NOT NULL REFERENCES zones(id),
    space_number INT            NOT NULL CHECK (space_number > 0),
    reason       VARCHAR(255)   NOT NULL,
    evidence_url TEXT,
    amount       DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    status       fine_status    NOT NULL DEFAULT 'unpaid',
    payment_id   INT            REFERENCES payments(id),
    issued_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    paid_at      TIMESTAMPTZ
);

CREATE INDEX idx_fines_vehicle_id ON fines(vehicle_id);
CREATE INDEX idx_fines_issued_by  ON fines(issued_by);
CREATE INDEX idx_fines_zone_id    ON fines(zone_id);
CREATE INDEX idx_fines_status     ON fines(status);

-- ─────────────────────────────────────────────
-- 14. ADMIN ACTION LOGS
-- Immutable audit trail — who did what, and when.
-- ─────────────────────────────────────────────
CREATE TABLE admin_action_logs (
    id         BIGSERIAL    PRIMARY KEY,
    admin_id   INT          NOT NULL REFERENCES users(id),
    action     VARCHAR(100) NOT NULL,
    details    TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_admin_action_logs_admin   ON admin_action_logs(admin_id);
CREATE INDEX idx_admin_action_logs_created ON admin_action_logs(created_at);

-- ─────────────────────────────────────────────
-- 15. updated_at TRIGGER
-- ─────────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_zones_updated_at
    BEFORE UPDATE ON zones
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_sessions_updated_at
    BEFORE UPDATE ON sessions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
