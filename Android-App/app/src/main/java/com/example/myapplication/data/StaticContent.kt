package com.example.myapplication.data

// Placeholder content for features that do not yet have a backing API/table:
// the municipality label and the zone fallback. Replace with real endpoints once
// those tables exist (see API "out of scope" notes). In-app notifications are now
// stored locally per-user in NotificationStore.
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
}
