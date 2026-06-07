-- =============================================================
-- E-PARK · Datos de prueba para el ADMIN de CARTAGO
-- =============================================================
-- El reporte de admin (/api/admin/reports/summary) filtra por la municipalidad
-- de las ZONAS. El admin de Cartago (admin@epark.cr) no tenía zonas/sesiones/
-- multas, por lo que su reporte salía en cero. Esta migración agrega datos
-- completos y AISLADOS para Cartago (zonas, drivers propios, vehículos, métodos
-- de pago, sesiones, pagos, multas y logs) sin tocar los datos existentes.
--
-- Resumen esperado del reporte de Cartago:
--   Sesiones totales : 13  (10 completadas + 2 activas + 1 vencida)
--   Ingresos         : ₡11 200  (pagos de sesiones completadas)
--   Multas emitidas  : 4   (2 pendientes + 2 pagadas)
--   Espacios activos : 2   (sesiones en curso)
--   Espacios totales : 250 (suma de zonas activas de Cartago)
-- =============================================================

-- ─────────────────────────────────────────────
-- Zonas de Cartago
-- ─────────────────────────────────────────────
INSERT INTO zones (municipality_id, name, description, latitude, longitude, total_spots, hourly_rate, open_hour, close_hour) VALUES
    (
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'Zona Centro Cartago',
        'Parqueo frente a la Basílica de Nuestra Señora de los Ángeles',
        9.86444400, -83.91944400,
        60, 500.00, 6, 22
    ),
    (
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'Zona Las Ruinas',
        'Parqueo costado de las Ruinas de Cartago',
        9.86490000, -83.91980000,
        40, 450.00, 6, 20
    ),
    (
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'Zona Basílica',
        'Parqueo sur de la Basílica de los Ángeles',
        9.86230000, -83.91250000,
        50, 400.00, 5, 22
    ),
    (
        (SELECT id FROM municipalities WHERE name = 'Cartago'),
        'Zona Mall Metrópoli',
        'Parqueo público cercano al Paseo Metrópoli',
        9.85800000, -83.93500000,
        100, 600.00, 8, 23
    );

-- ─────────────────────────────────────────────
-- Drivers de prueba para Cartago
-- password: Driver1234!
-- ─────────────────────────────────────────────
INSERT INTO users (role_id, municipality_id, full_name, email, password_hash, phone, email_verified) VALUES
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'Diego Brenes Solano',   'diego.cartago@gmail.com', 'Driver1234!', '+50688002001', TRUE),
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'María Fernández Mora',  'maria.cartago@gmail.com', 'Driver1234!', '+50688002002', TRUE),
    ((SELECT id FROM roles WHERE name = 'driver'), NULL, 'José Ramírez Quirós',   'jose.cartago@gmail.com',  'Driver1234!', '+50688002003', TRUE);

-- ─────────────────────────────────────────────
-- Vehículos (1 por driver)
-- ─────────────────────────────────────────────
INSERT INTO vehicles (user_id, vehicle_type_id, plate, brand, model, color) VALUES
    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'), 1, 'CTG-1001', 'Toyota',     'Hilux',   'Gris'),
    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'), 1, 'CTG-2002', 'Hyundai',    'Elantra', 'Blanco'),
    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),  1, 'CTG-3003', 'Volkswagen', 'Jetta',   'Negro');

-- ─────────────────────────────────────────────
-- Métodos de pago (1 por driver, default)
-- ─────────────────────────────────────────────
INSERT INTO payment_methods (user_id, card_brand, last_four, expiry_month, expiry_year, token, is_default) VALUES
    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'), 'Visa',       '1001', 10, 28, 'tok_test_diego_visa', TRUE),
    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'), 'Mastercard', '2002',  4, 27, 'tok_test_maria_mc',   TRUE),
    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),  'Visa',       '3003',  8, 29, 'tok_test_jose_visa',  TRUE);

-- ─────────────────────────────────────────────
-- Sesiones en zonas de Cartago
-- (completadas distribuidas en los últimos 30 días, 2 activas y 1 vencida)
-- ─────────────────────────────────────────────
INSERT INTO sessions (user_id, vehicle_id, zone_id, space_number, scheduled_start, scheduled_end, actual_end, hourly_rate, total_cost, status) VALUES
    -- Completadas
    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Centro Cartago' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     5,
     NOW() - INTERVAL '2 days'  + TIME '08:00:00', NOW() - INTERVAL '2 days'  + TIME '10:00:00', NOW() - INTERVAL '2 days'  + TIME '10:00:00',
     500.00, 1000.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Mall Metrópoli' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     12,
     NOW() - INTERVAL '9 days'  + TIME '14:00:00', NOW() - INTERVAL '9 days'  + TIME '16:00:00', NOW() - INTERVAL '9 days'  + TIME '16:00:00',
     600.00, 1200.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-2002'),
     (SELECT id FROM zones WHERE name = 'Zona Las Ruinas' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     3,
     NOW() - INTERVAL '1 day'   + TIME '09:00:00', NOW() - INTERVAL '1 day'   + TIME '11:00:00', NOW() - INTERVAL '1 day'   + TIME '11:00:00',
     450.00, 900.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-2002'),
     (SELECT id FROM zones WHERE name = 'Zona Basílica' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     7,
     NOW() - INTERVAL '5 days'  + TIME '10:00:00', NOW() - INTERVAL '5 days'  + TIME '13:00:00', NOW() - INTERVAL '5 days'  + TIME '13:00:00',
     400.00, 1200.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-3003'),
     (SELECT id FROM zones WHERE name = 'Zona Centro Cartago' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     9,
     NOW() - INTERVAL '3 days'  + TIME '07:30:00', NOW() - INTERVAL '3 days'  + TIME '09:30:00', NOW() - INTERVAL '3 days'  + TIME '09:30:00',
     500.00, 1000.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-3003'),
     (SELECT id FROM zones WHERE name = 'Zona Mall Metrópoli' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     22,
     NOW() - INTERVAL '7 days'  + TIME '12:00:00', NOW() - INTERVAL '7 days'  + TIME '15:00:00', NOW() - INTERVAL '7 days'  + TIME '15:00:00',
     600.00, 1800.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Basílica' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     14,
     NOW() - INTERVAL '12 days' + TIME '16:00:00', NOW() - INTERVAL '12 days' + TIME '18:00:00', NOW() - INTERVAL '12 days' + TIME '18:00:00',
     400.00, 800.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-2002'),
     (SELECT id FROM zones WHERE name = 'Zona Centro Cartago' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     18,
     NOW() - INTERVAL '15 days' + TIME '08:00:00', NOW() - INTERVAL '15 days' + TIME '09:30:00', NOW() - INTERVAL '15 days' + TIME '09:30:00',
     500.00, 750.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-3003'),
     (SELECT id FROM zones WHERE name = 'Zona Las Ruinas' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     6,
     NOW() - INTERVAL '20 days' + TIME '11:00:00', NOW() - INTERVAL '20 days' + TIME '14:00:00', NOW() - INTERVAL '20 days' + TIME '14:00:00',
     450.00, 1350.00, 'completed'),

    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Mall Metrópoli' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     30,
     NOW() - INTERVAL '26 days' + TIME '09:00:00', NOW() - INTERVAL '26 days' + TIME '11:00:00', NOW() - INTERVAL '26 days' + TIME '11:00:00',
     600.00, 1200.00, 'completed'),

    -- Activa en curso (cuenta como espacio ocupado)
    ((SELECT id FROM users WHERE email = 'maria.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-2002'),
     (SELECT id FROM zones WHERE name = 'Zona Centro Cartago' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     40,
     NOW() - INTERVAL '1 hour', NOW() + INTERVAL '1 hour', NULL,
     500.00, 0.00, 'active'),

    -- Activa vencida (genera notificación "session_overdue" para el admin)
    ((SELECT id FROM users WHERE email = 'jose.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-3003'),
     (SELECT id FROM zones WHERE name = 'Zona Las Ruinas' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     15,
     NOW() - INTERVAL '3 hours', NOW() - INTERVAL '1 hour', NULL,
     450.00, 0.00, 'active'),

    -- Sesión vencida sin pago
    ((SELECT id FROM users WHERE email = 'diego.cartago@gmail.com'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Basílica' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     50,
     NOW() - INTERVAL '10 days' + TIME '09:00:00', NOW() - INTERVAL '10 days' + TIME '10:00:00', NULL,
     400.00, 400.00, 'expired');

-- ─────────────────────────────────────────────
-- Pagos de las sesiones completadas de Cartago
-- (solo afecta sesiones de zonas de Cartago)
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
WHERE s.status = 'completed'
  AND s.zone_id IN (
      SELECT id FROM zones WHERE municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')
  );

-- ─────────────────────────────────────────────
-- Multas en zonas de Cartago (emitidas por el admin de Cartago)
-- ─────────────────────────────────────────────
INSERT INTO fines (issued_by, vehicle_id, zone_id, space_number, reason, amount, status, issued_at, paid_at) VALUES
    -- Pendientes
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Centro Cartago' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     5, 'Tiempo de parqueo vencido', 5000.00, 'unpaid', NOW() - INTERVAL '3 days' + TIME '10:30:00', NULL),

    ((SELECT id FROM users WHERE email = 'admin@epark.cr'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-2002'),
     (SELECT id FROM zones WHERE name = 'Zona Las Ruinas' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     3, 'Vehículo en espacio no autorizado', 8000.00, 'unpaid', NOW() - INTERVAL '1 day' + TIME '11:15:00', NULL),

    -- Pagadas (generan notificación "fine_paid" para el admin)
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-3003'),
     (SELECT id FROM zones WHERE name = 'Zona Mall Metrópoli' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     22, 'Tiempo de parqueo vencido', 5000.00, 'paid', NOW() - INTERVAL '14 days' + TIME '15:30:00', NOW() - INTERVAL '12 days' + TIME '09:00:00'),

    ((SELECT id FROM users WHERE email = 'admin@epark.cr'),
     (SELECT id FROM vehicles WHERE plate = 'CTG-1001'),
     (SELECT id FROM zones WHERE name = 'Zona Basílica' AND municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')),
     14, 'Doble fila en zona de parqueo', 10000.00, 'paid', NOW() - INTERVAL '18 days' + TIME '09:15:00', NOW() - INTERVAL '16 days' + TIME '10:00:00');

-- ─────────────────────────────────────────────
-- Pagos de las multas pagadas de Cartago
-- ─────────────────────────────────────────────
INSERT INTO payments (user_id, payment_method_id, amount, status, reference_type, reference_id, paid_at)
SELECT
    v.user_id,
    pm.id,
    f.amount,
    'completed',
    'fine',
    f.id,
    f.paid_at
FROM fines f
JOIN vehicles v ON v.id = f.vehicle_id
JOIN payment_methods pm ON pm.user_id = v.user_id AND pm.is_default = TRUE
WHERE f.status = 'paid'
  AND f.zone_id IN (
      SELECT id FROM zones WHERE municipality_id = (SELECT id FROM municipalities WHERE name = 'Cartago')
  );

-- ─────────────────────────────────────────────
-- Logs de acciones del admin de Cartago (pantalla de bitácora)
-- ─────────────────────────────────────────────
INSERT INTO admin_action_logs (admin_id, action, details, created_at) VALUES
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'), 'zone.create', 'Creó la zona Zona Centro Cartago',  NOW() - INTERVAL '28 days'),
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'), 'zone.create', 'Creó la zona Zona Mall Metrópoli',  NOW() - INTERVAL '28 days'),
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'), 'fine.issue',  'Emitió multa a la placa CTG-1001',  NOW() - INTERVAL '18 days'),
    ((SELECT id FROM users WHERE email = 'admin@epark.cr'), 'fine.issue',  'Emitió multa a la placa CTG-3003',  NOW() - INTERVAL '14 days');
