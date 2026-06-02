package com.example.myapplication.data

// Placeholder content for features that do not yet have a backing API/table:
// in-app notifications, admin alerts, and the municipality label. Replace with
// real endpoints once those tables exist (see API "out of scope" notes).
object StaticContent {

    const val municipality = "Cartago"

    // Shown only if a zone-scoped screen is opened without a zone selected first.
    val placeholderZone = ParkingZone(
        id = "",
        municipalityId = 0,
        municipalityName = "",
        name = "Selecciona una zona",
        rate = "",
        hours = "",
        totalSpots = 0,
        freeSpots = 0,
        isActive = true,
    )

    val notifications = listOf(
        NotificationItem("n1", "Sesión por vencer", "Tu sesión en Zona Las Ruinas vence en 10 minutos.", "9:41 AM"),
        NotificationItem("n2", "Multa emitida", "Se emitió una multa para la placa ABC-123 en Zona Las Ruinas.", "9:41 AM"),
    )
}
