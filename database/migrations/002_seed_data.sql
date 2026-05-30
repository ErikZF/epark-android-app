-- =============================================================
-- E-PARK · Seed Data
-- Migration: 002_seed_data
-- Run AFTER 001_initial_schema.sql
-- =============================================================

-- ─────────────────────────────────────────────
-- Roles
-- ─────────────────────────────────────────────
INSERT INTO roles (name) VALUES
    ('admin'),
    ('driver');

-- ─────────────────────────────────────────────
-- Vehicle types
-- ─────────────────────────────────────────────
INSERT INTO vehicle_types (name) VALUES
    ('car'),
    ('motorcycle'),
    ('truck');

-- ─────────────────────────────────────────────
-- Municipalities
-- ─────────────────────────────────────────────
INSERT INTO municipalities (name, country) VALUES
    ('San José',   'Costa Rica'),
    ('Heredia',    'Costa Rica'),
    ('Alajuela',   'Costa Rica'),
    ('Cartago',    'Costa Rica'),
    ('Liberia',    'Costa Rica');

-- ─────────────────────────────────────────────
-- Default admin user
-- password: Admin1234! (bcrypt hash – replace in production)
-- ─────────────────────────────────────────────
INSERT INTO users (role_id, municipality_id, full_name, email, password_hash, phone) VALUES
    (
        (SELECT id FROM roles WHERE name = 'driver'),
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'E-Park Driver',
        'nei@hotmail.com',
        '123',
        '+50688880000'
    );

INSERT INTO users (role_id, municipality_id, full_name, email, password_hash, phone) VALUES
    (
        (SELECT id FROM roles WHERE name = 'admin'),
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'E-Park Admin',
        'admin@epark.cr',
        '$2a$12$placeholder_hash_replace_in_prod',
        '+50688880000'
    );

-- ─────────────────────────────────────────────
-- Sample zones (San José)
-- ─────────────────────────────────────────────
INSERT INTO zones (municipality_id, name, description, latitude, longitude, total_spots, hourly_rate) VALUES
    (
        (SELECT id FROM municipalities WHERE name = 'San José'),
        'Zona Centro',
        'Parqueo central frente al Mercado Central',
        9.93333300, -84.08333300,
        80, 500.00
    ),
    (
        (SELECT id FROM municipalities WHERE name = 'San José'),
        'Zona Sabana',
        'Parqueo costado este del Parque La Sabana',
        9.93527800, -84.10194400,
        50, 400.00
    ),
    (
        (SELECT id FROM municipalities WHERE name = 'Heredia'),
        'Zona Central Heredia',
        'Parqueo municipal frente al parque central de Heredia',
        9.99888900, -84.11694400,
        40, 350.00
    );
