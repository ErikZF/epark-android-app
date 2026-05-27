package com.example.myapplication.data

object FakeData {

    val zones = listOf(
        ParkingZone("1", "Zona Las Ruinas", "₡500/hr", "6:00 am - 24:00pm", 26, 12, true),
        ParkingZone("2", "Zona Mercado", "₡750/hr", "6:00 am - 24:00pm", 20, 3, true),
        ParkingZone("3", "Zona Cultural", "₡700/hr", "6:00 am - 24:00pm", 15, 0, true),
        ParkingZone("4", "Zona Basílica", "₡1000/hr", "6:00 am - 24:00pm", 30, 7, false),
        ParkingZone("5", "Zona Centro", "₡1200/hr", "6:00 am - 24:00pm", 55, 12, true),
    )

    val vehicles = listOf(
        Vehicle("ABC-123", "BMW", "M3", "2022"),
        Vehicle("DEF-456", "BMW", "M4", "2023"),
    )

    val paymentMethods = mutableListOf(
        PaymentMethod("pm1", "Visa", "4521", "09/27", isDefault = true),
        PaymentMethod("pm2", "Visa", "4322", "04/27", isDefault = false),
    )

    val sessions = listOf(
        ParkingSession("s1", "Zona Las Ruinas", "#0042", "ABC-123", "28 abr 02: 05", "02:05", "₡1500", "Pagado"),
        ParkingSession("s2", "Zona Mercado", "#0015", "ABC-123", "28 abr 02: 05", "02:05", "₡1500", "Pagado"),
        ParkingSession("s3", "Zona Cultural", "#0031", "ABC-123", "28 abr 02: 05", "02:05", "₡1500", "Pagado"),
        ParkingSession("s4", "Zona Centro", "#0042", "ABC-123", "28 abr 02: 05", "02:05", "₡1500", "Pagado"),
    )

    val userFines = listOf(
        Fine("f1", "Zona Las Ruinas", "#0086", "ABC-123", "25 abr 2026", "₡1500", isPaid = false),
        Fine("f2", "Zona Mercado", "#0042", "ABC-123", "15 abr 2026", "₡1500", isPaid = true),
    )

    val adminFines = listOf(
        Fine("af1", "Zona Las Ruinas", "#0086", "ABC-123", "25 abr 2026", "₡1500", isPaid = false),
        Fine("af2", "Zona Las Ruinas", "#0086", "AFC-123", "25 abr 2026", "₡1500", isPaid = false),
        Fine("af3", "Zona Mercado", "#0086", "ABF-221", "25 abr 2026", "₡1500", isPaid = true),
    )

    val notifications = listOf(
        NotificationItem("n1", "Sesión por vencer", "Tu sesión en Zona Las Ruinas vence en 10 minutos.", "9:41 AM"),
        NotificationItem("n2", "Multa emitida", "Se emitió una multa para la placa ABC-123 en Zona Las Ruinas.", "9:41 AM"),
    )

    val adminAlerts = listOf(
        AdminAlert("a1", "LOREM IPSUM", "Lorem ipsum dolor sit", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris lacinia ullamcorper leo, a ultrices purus venenatis sit amet.", "4min ago"),
        AdminAlert("a2", "LOREM IPSUM", "Lorem ipsum dolor sit", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris lacinia ullamcorper leo, a ultrices purus venenatis sit amet. Donec porta elementum.", "4min ago"),
    )

    val reportSummary = AdminReportSummary(
        totalSessions = 124,
        revenue = "₡150.000",
        finesIssued = 4,
        activeSpots = "43/55",
        date = "Feb 2024",
    )

    val activeSession = ParkingSession(
        "as1", "Zona Centro", "#0042", "ABC-123", "Hoy", "03:40", "₡123", "Activa"
    )

    const val userName = "Erik Zhou"
    const val userEmail = "e.zhou.1@example.com"
    const val userInitials = "EZ"
    const val userSessions = 47
    const val userPaid = 45
    const val userFinesCount = 2
    const val municipality = "Cartago"
    const val adminEmail = "admin@example.com"
}
