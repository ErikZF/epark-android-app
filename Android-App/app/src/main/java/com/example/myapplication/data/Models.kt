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
    // UTC hours; openHour 0-23, closeHour 1-24 (24 = midnight of next day)
    val openHour: Int = 6,
    val closeHour: Int = 22,
    val isActive: Boolean = true,
)

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
    val amount: String,
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
)

data class AdminReportSummary(
    val totalSessions: Int,
    val revenue: String,
    val finesIssued: Int,
    val activeSpots: String,
    val date: String,
)
