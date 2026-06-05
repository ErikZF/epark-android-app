-- =============================================================
-- E-PARK · Seed & Test Data
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
INSERT INTO users (role_id, municipality_id, full_name, email, password_hash, phone, email_verified) VALUES
(
    (SELECT id FROM roles WHERE name = 'admin'),
    (SELECT id FROM municipalities WHERE name = 'Cartago'),
    'E-Park Admin',
    'admin@epark.cr',
    '$2a$12$placeholder_hash_replace_in_prod',
    '+50688880000',
    TRUE
);

-- ─────────────────────────────────────────────
-- Sample zones (San José & Heredia)
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

-- ─────────────────────────────────────────────
-- Test driver users
-- password: Driver1234!
-- ─────────────────────────────────────────────
INSERT INTO users (role_id, municipality_id, full_name, email, password_hash, phone, email_verified) VALUES
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'Carlos Mora Jiménez',   'carlos.mora@gmail.com',   'Driver1234!', '+50688001001', TRUE),
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'Valentina Ríos Solano', 'vrios@hotmail.com',       'Driver1234!', '+50688001002', TRUE),
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'Andrés Quesada Vega',   'andres.q@gmail.com',      'Driver1234!', '+50688001003', TRUE),
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'Sofía Herrera López',   'sofia.herrera@gmail.com', 'Driver1234!', '+50688001004', TRUE);

-- ─────────────────────────────────────────────
-- Vehicles (2 per driver)
-- ─────────────────────────────────────────────
INSERT INTO vehicles (user_id, vehicle_type_id, plate, brand, model, color) VALUES
    -- Carlos
    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),   1, 'CRC-1234', 'Toyota',  'Corolla', 'Blanco'),
    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),   2, 'CRC-5678', 'Honda',   'CB500',   'Negro'),
    -- Valentina
    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),       1, 'SJO-8821', 'Hyundai', 'Tucson',  'Gris'),
    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),       1, 'SJO-4410', 'Kia',     'Picanto', 'Rojo'),
    -- Andrés
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),      1, 'HER-3301', 'Nissan',  'Sentra',  'Azul'),
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),      1, 'HER-7762', 'Suzuki',  'Swift',   'Plateado'),
    -- Sofía
    ((SELECT id FROM users WHERE email = 'sofia.herrera@gmail.com'), 1, 'CAR-9981', 'Mazda',   'CX-5',    'Blanco'),
    ((SELECT id FROM users WHERE email = 'sofia.herrera@gmail.com'), 2, 'CAR-1150', 'Yamaha',  'MT-07',   'Negro');

-- ─────────────────────────────────────────────
-- Payment methods (1-2 per driver)
-- ─────────────────────────────────────────────
INSERT INTO payment_methods (user_id, card_brand, last_four, expiry_month, expiry_year, token, is_default) VALUES
    -- Carlos
    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),   'Visa',       '4321', 12, 27, 'tok_test_carlos_visa', TRUE),
    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),   'Mastercard', '8899',  6, 26, 'tok_test_carlos_mc',   FALSE),
    -- Valentina
    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),       'Visa',       '1122',  3, 28, 'tok_test_valen_visa',  TRUE),
    -- Andrés
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),      'Mastercard', '5544',  9, 27, 'tok_test_andres_mc',   TRUE),
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),      'Visa',       '7733', 11, 29, 'tok_test_andres_visa', FALSE),
    -- Sofía
    ((SELECT id FROM users WHERE email = 'sofia.herrera@gmail.com'), 'Visa',       '6601',  1, 28, 'tok_test_sofia_visa',  TRUE);

-- ─────────────────────────────────────────────
-- Completed sessions (spread across last 30 days)
-- zone 1 = Zona Centro SJ, zone 2 = Zona Sabana SJ, zone 3 = Zona Central Heredia
-- ─────────────────────────────────────────────
INSERT INTO sessions (user_id, vehicle_id, zone_id, space_number, scheduled_start, scheduled_end, actual_end, hourly_rate, total_cost, status) VALUES
    -- Carlos
    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CRC-1234'),
     1, 5,
     NOW() - INTERVAL '2 days'  + TIME '09:00:00',
     NOW() - INTERVAL '2 days'  + TIME '11:00:00',
     NOW() - INTERVAL '2 days'  + TIME '11:00:00',
     500.00, 1000.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'carlos.mora@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CRC-1234'),
     2, 12,
     NOW() - INTERVAL '5 days'  + TIME '14:00:00',
     NOW() - INTERVAL '5 days'  + TIME '16:30:00',
     NOW() - INTERVAL '5 days'  + TIME '16:30:00',
     400.00, 1000.00, 'completed'),

    -- Valentina
    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'SJO-8821'),
     2, 3,
     NOW() - INTERVAL '1 day'   + TIME '10:00:00',
     NOW() - INTERVAL '1 day'   + TIME '12:00:00',
     NOW() - INTERVAL '1 day'   + TIME '12:00:00',
     400.00, 800.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'SJO-4410'),
     1, 7,
     NOW() - INTERVAL '8 days'  + TIME '08:30:00',
     NOW() - INTERVAL '8 days'  + TIME '09:30:00',
     NOW() - INTERVAL '8 days'  + TIME '09:30:00',
     500.00, 500.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'vrios@hotmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'SJO-8821'),
     3, 2,
     NOW() - INTERVAL '12 days' + TIME '11:00:00',
     NOW() - INTERVAL '12 days' + TIME '14:00:00',
     NOW() - INTERVAL '12 days' + TIME '14:00:00',
     350.00, 1050.00, 'completed'),

    -- Andrés
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'HER-3301'),
     3, 10,
     NOW() - INTERVAL '3 days'  + TIME '07:00:00',
     NOW() - INTERVAL '3 days'  + TIME '09:00:00',
     NOW() - INTERVAL '3 days'  + TIME '09:00:00',
     350.00, 700.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'HER-7762'),
     1, 15,
     NOW() - INTERVAL '7 days'  + TIME '13:00:00',
     NOW() - INTERVAL '7 days'  + TIME '15:00:00',
     NOW() - INTERVAL '7 days'  + TIME '15:00:00',
     500.00, 1000.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'HER-3301'),
     2, 20,
     NOW() - INTERVAL '15 days' + TIME '16:00:00',
     NOW() - INTERVAL '15 days' + TIME '18:00:00',
     NOW() - INTERVAL '15 days' + TIME '18:00:00',
     400.00, 800.00, 'completed'),

    -- Sofía
    ((SELECT id FROM users WHERE email = 'sofia.herrera@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CAR-9981'),
     1, 22,
     NOW() - INTERVAL '1 day'   + TIME '09:00:00',
     NOW() - INTERVAL '1 day'   + TIME '10:30:00',
     NOW() - INTERVAL '1 day'   + TIME '10:30:00',
     500.00, 750.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'sofia.herrera@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CAR-9981'),
     3, 8,
     NOW() - INTERVAL '20 days' + TIME '10:00:00',
     NOW() - INTERVAL '20 days' + TIME '13:00:00',
     NOW() - INTERVAL '20 days' + TIME '13:00:00',
     350.00, 1050.00, 'completed'),

    -- Expired session (no payment)
    ((SELECT id FROM users WHERE email = 'andres.q@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'HER-7762'),
     1, 30,
     NOW() - INTERVAL '10 days' + TIME '09:00:00',
     NOW() - INTERVAL '10 days' + TIME '10:00:00',
     NULL,
     500.00, 500.00, 'expired');

-- ─────────────────────────────────────────────
-- Payments for completed sessions
-- ─────────────────────────────────────────────
INSERT INTO payments (user_id, payment_method_id, amount, status, reference_type, reference_id, paid_at)
SELECT
    s.user_id,
    pm.id,
    s.total_cost,
    'completed',
    'session',
    s.id,
    s.actual_end
FROM sessions s
JOIN payment_methods pm ON pm.user_id = s.user_id AND pm.is_default = TRUE
WHERE s.status = 'completed';

-- ─────────────────────────────────────────────
-- Fines
-- issued_by = admin user (id 1 from seed)
-- ─────────────────────────────────────────────
INSERT INTO fines (issued_by, vehicle_id, zone_id, space_number, reason, amount, status, issued_at) VALUES
    -- Unpaid
    (1, (SELECT id FROM vehicles WHERE plate = 'CRC-1234'), 1,  5, 'Tiempo de parqueo vencido',          5000.00, 'unpaid', NOW() - INTERVAL '3 days' + TIME '11:30:00'),
    (1, (SELECT id FROM vehicles WHERE plate = 'HER-3301'), 3, 10, 'Vehículo en espacio no autorizado',  8000.00, 'unpaid', NOW() - INTERVAL '6 days' + TIME '14:00:00'),
    (1, (SELECT id FROM vehicles WHERE plate = 'SJO-8821'), 2,  3, 'Tiempo de parqueo vencido',          5000.00, 'unpaid', NOW() - INTERVAL '1 day'  + TIME '16:45:00'),
    -- Paid
    (1, (SELECT id FROM vehicles WHERE plate = 'CAR-9981'), 1, 22, 'Tiempo de parqueo vencido',          5000.00, 'paid',   NOW() - INTERVAL '25 days' + TIME '10:00:00'),
    (1, (SELECT id FROM vehicles WHERE plate = 'HER-7762'), 3, 15, 'Doble fila en zona de parqueo',     10000.00, 'paid',   NOW() - INTERVAL '18 days' + TIME '09:15:00');

-- ─────────────────────────────────────────────
-- Payments for paid fines
-- ─────────────────────────────────────────────
INSERT INTO payments (user_id, payment_method_id, amount, status, reference_type, reference_id, paid_at)
SELECT
    v.user_id,
    pm.id,
    f.amount,
    'completed',
    'fine',
    f.id,
    f.issued_at + INTERVAL '2 days'
FROM fines f
JOIN vehicles v  ON v.id  = f.vehicle_id
JOIN payment_methods pm ON pm.user_id = v.user_id AND pm.is_default = TRUE
WHERE f.status = 'paid';

UPDATE fines
SET paid_at = issued_at + INTERVAL '2 days'
WHERE status = 'paid';
