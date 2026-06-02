package com.example.myapplication.data

enum class UserRole { RESIDENT, ADMIN }

data class ParkingZone(
    val id: String,
    val municipalityId: Int,
    val municipalityName: String,
    val name: String,
    val rate: String,
    val hours: String,
    val totalSpots: Int,
    val freeSpots: Int,
    // Local (Costa Rica) hours; openHour 0-23, closeHour 1-24 (24 = midnight of next day)
    val openHour: Int = 6,
    val closeHour: Int = 22,
    val isActive: Boolean = true,
    val hourlyRate: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) {
    /** True when the zone has usable coordinates (not the default/missing 0,0 "null island"). */
    val hasCoordinates: Boolean get() = latitude != 0.0 || longitude != 0.0
}

data class ActiveSession(
    val id: Int,
    val zoneName: String,
    val zoneOpenHour: Int,
    val zoneCloseHour: Int,
    val plate: String,
    val spaceNumber: Int,
    val scheduledStartMs: Long,
    val scheduledEndMs: Long,
    val hourlyRate: Double,
)

data class Vehicle(
    val id: Int,
    val plate: String,
    val brand: String,
    val model: String,
    val year: String,
)

data class PaymentMethod(
    val id: String,
    val label: String,
    val last4: String,
    val expiry: String,
    val isDefault: Boolean = false,
)

data class ParkingSession(
    val id: String,
    val zoneName: String,
    val spaceNumber: String,
    val plate: String,
    val date: String,
    val duration: String,
    val total: String,
    val status: String,
)

data class Fine(
    val id: String,
    val zoneName: String,
    val spaceNumber: String,
    val plate: String,
    val date: String,
    val time: String,
    val reason: String,
    val amount: String,
    val amountRaw: Double,
    val isPaid: Boolean,
)

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
)

data class AdminAlert(
    val id: String,
    val source: String,
    val title: String,
    val body: String,
    val time: String,
    // "session_overdue" | "fine_paid"
    val type: String = "",
    val referenceId: Int = 0,
    val plate: String = "",
    val zoneName: String = "",
    val amount: String? = null,
    val spaceNumber: String? = null,
)

data class ZoneRevenue(val zoneName: String, val revenue: String, val sessions: Int)

data class AdminReportSummary(
    val totalSessions: Int,
    val revenue: String,
    val finesIssued: Int,
    val activeSpots: String,
    val date: String,
    val revenueByZone: List<ZoneRevenue> = emptyList(),
)

data class AdminActionLog(
    val id: String,
    val adminName: String,
    val action: String,
    val details: String,
    val date: String,
    val time: String,
)
