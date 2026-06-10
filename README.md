# epark-android-app
Native Android application for urban municipal parking management, allowing drivers to manage parking sessions and municipal administrators to supervise operations remotely.


datos de pruebas


docker exec epark-postgres psql -U postgres -d epark -c "
INSERT INTO sessions (user_id, vehicle_id, zone_id, space_number, scheduled_start, scheduled_end, hourly_rate, total_cost, status)
SELECT u.id, v.id, 1, 77, NOW(), NOW() + INTERVAL '11 minutes', z.hourly_rate, 0, 'active'
FROM users u
JOIN vehicles v ON v.user_id = u.id
JOIN zones z ON z.id = 1
WHERE u.email = 'diego.cartago@gmail.com'
LIMIT 1;"


docker exec epark-postgres psql -U postgres -d epark -c "
INSERT INTO sessions (user_id, vehicle_id, zone_id, space_number, scheduled_start, scheduled_end, hourly_rate, total_cost, status)
SELECT u.id, v.id, 1, 77, NOW(), NOW() + INTERVAL '11 minutes', z.hourly_rate, 0, 'active'
FROM users u
JOIN vehicles v ON v.user_id = u.id
JOIN zones z ON z.id = 1
WHERE u.email = 'erikzf025@gmail.com'
LIMIT 1;"

agregar zonas

9.857229750850344, -83.91112147558349
