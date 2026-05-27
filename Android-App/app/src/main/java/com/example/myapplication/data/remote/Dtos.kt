package com.example.myapplication.data.remote

// Mirrors the JSON contracts exposed by the .NET API (System.Text.Json camelCase).

// ── Auth ─────────────────────────────────────────────
data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val plate: String? = null,
    val vehicleTypeId: Short? = null,
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null,
)

data class LoginRequestDto(val email: String, val password: String)

data class AuthResponseDto(
    val userId: Int,
    val fullName: String,
    val email: String,
    val role: String,
)

// ── Zones ────────────────────────────────────────────
data class ZoneDto(
    val id: Int,
    val municipalityId: Int,
    val municipalityName: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val totalSpots: Int,
    val freeSpots: Int,
    val hourlyRate: Double,
    val isActive: Boolean,
)

data class CreateZoneRequestDto(
    val municipalityId: Int,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val totalSpots: Int,
    val hourlyRate: Double,
)

data class UpdateZoneRequestDto(
    val name: String,
    val description: String?,
    val totalSpots: Int,
    val hourlyRate: Double,
    val isActive: Boolean,
)

// ── Vehicles ─────────────────────────────────────────
data class VehicleDto(
    val id: Int,
    val userId: Int,
    val vehicleTypeId: Int,
    val vehicleType: String,
    val plate: String,
    val nickname: String?,
    val brand: String?,
    val model: String?,
    val color: String?,
    val isActive: Boolean,
)

data class CreateVehicleRequestDto(
    val userId: Int,
    val vehicleTypeId: Short,
    val plate: String,
    val nickname: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null,
)

// ── Payment methods ──────────────────────────────────
data class PaymentMethodDto(
    val id: Int,
    val cardBrand: String?,
    val lastFour: String,
    val expiryMonth: Short,
    val expiryYear: Short,
    val isDefault: Boolean,
)

data class CreatePaymentMethodRequestDto(
    val userId: Int,
    val cardBrand: String?,
    val lastFour: String,
    val expiryMonth: Short,
    val expiryYear: Short,
    val token: String,
    val isDefault: Boolean,
)

// ── Sessions ─────────────────────────────────────────
data class CreateSessionRequestDto(
    val userId: Int,
    val vehicleId: Int,
    val zoneId: Int,
    val scheduledStart: String,
    val scheduledEnd: String,
)

data class ExtendSessionRequestDto(val addedMinutes: Int)

data class SessionDto(
    val id: Int,
    val zoneId: Int,
    val zoneName: String,
    val vehicleId: Int,
    val plate: String,
    val scheduledStart: String,
    val scheduledEnd: String,
    val actualEnd: String?,
    val hourlyRate: Double,
    val totalCost: Double,
    val status: String,
)

// ── Payments ─────────────────────────────────────────
data class CreatePaymentRequestDto(
    val userId: Int,
    val paymentMethodId: Int?,
    val amount: Double,
    val referenceType: String,
    val referenceId: Int,
)

data class PaymentDto(
    val id: Int,
    val amount: Double,
    val currency: String,
    val status: String,
    val referenceType: String,
    val referenceId: Int,
    val paidAt: String?,
    val invoiceNumber: String?,
)

// ── Fines ────────────────────────────────────────────
data class FineDto(
    val id: Int,
    val vehicleId: Int,
    val plate: String,
    val zoneId: Int,
    val zoneName: String,
    val reason: String,
    val evidenceUrl: String?,
    val amount: Double,
    val status: String,
    val issuedAt: String,
    val paidAt: String?,
)

data class CreateFineRequestDto(
    val issuedBy: Int,
    val vehicleId: Int,
    val zoneId: Int,
    val reason: String,
    val evidenceUrl: String?,
    val amount: Double,
)

// ── Reports ──────────────────────────────────────────
data class ReportSummaryDto(
    val totalSessions: Int,
    val revenue: Double,
    val finesIssued: Int,
    val activeSpots: Int,
    val totalSpots: Int,
)
