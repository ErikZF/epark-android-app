package com.example.myapplication.data.repository

import com.example.myapplication.data.AdminReportSummary
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.remote.CreateFineRequestDto
import com.example.myapplication.data.remote.CreatePaymentRequestDto
import com.example.myapplication.data.remote.CreateSessionRequestDto
import com.example.myapplication.data.remote.CreateVehicleRequestDto
import com.example.myapplication.data.remote.CreateZoneRequestDto
import com.example.myapplication.data.remote.EparkApi
import com.example.myapplication.data.remote.ExtendSessionRequestDto
import com.example.myapplication.data.remote.LoginRequestDto
import com.example.myapplication.data.remote.RegisterRequestDto

private val api: EparkApi get() = ApiClient.api

class AuthRepository {
    /** Logs in and stores the session in [AuthState]. Returns the role. */
    suspend fun login(email: String, password: String): String {
        val auth = api.login(LoginRequestDto(email.trim(), password))
        AuthState.set(auth)
        return auth.role
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        plate: String? = null,
    ): String {
        val auth = api.register(
            RegisterRequestDto(fullName = fullName, email = email.trim(), password = password, plate = plate)
        )
        AuthState.set(auth)
        return auth.role
    }
}

class ZoneRepository {
    suspend fun getZones(): List<ParkingZone> = api.getZones().map { it.toDomain() }

    suspend fun addZone(
        municipalityId: Int,
        name: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        totalSpots: Int,
        hourlyRate: Double,
    ) = api.createZone(
        CreateZoneRequestDto(
            municipalityId,
            name,
            description,
            latitude,
            longitude,
            totalSpots,
            hourlyRate
        )
    )
}

class VehicleRepository {
    suspend fun getVehicles(userId: Int = AuthState.userId): List<Vehicle> =
        api.getVehicles(userId).map { it.toDomain() }

    suspend fun addVehicle(plate: String, vehicleTypeId: Short, brand: String?, model: String?) {
        api.createVehicle(
            CreateVehicleRequestDto(
                userId = AuthState.userId,
                vehicleTypeId = vehicleTypeId,
                plate = plate,
                brand = brand,
                model = model,
            )
        )
    }
}

class SessionRepository {
    suspend fun getHistory(userId: Int = AuthState.userId): List<ParkingSession> =
        api.getSessions(userId).map { it.toDomain() }

    suspend fun startSession(vehicleId: Int, zoneId: Int, startIso: String, endIso: String) {
        api.createSession(
            CreateSessionRequestDto(AuthState.userId, vehicleId, zoneId, startIso, endIso)
        )
    }

    suspend fun extend(sessionId: Int, addedMinutes: Int) {
        api.extendSession(sessionId, ExtendSessionRequestDto(addedMinutes))
    }

    suspend fun finalize(sessionId: Int) = api.finalizeSession(sessionId)
}

class PaymentRepository {
    suspend fun getMethods(userId: Int = AuthState.userId): List<PaymentMethod> =
        api.getPaymentMethods(userId).map { it.toDomain() }

    suspend fun pay(amount: Double, referenceType: String, referenceId: Int, paymentMethodId: Int? = null) {
        api.createPayment(
            CreatePaymentRequestDto(AuthState.userId, paymentMethodId, amount, referenceType, referenceId)
        )
    }
}

class FineRepository {
    suspend fun getUserFines(userId: Int = AuthState.userId): List<Fine> =
        api.getUserFines(userId).map { it.toDomain() }

    suspend fun getAllFines(): List<Fine> = api.getAllFines().map { it.toDomain() }

    suspend fun issueFine(vehicleId: Int, zoneId: Int, reason: String, amount: Double) {
        api.createFine(
            CreateFineRequestDto(AuthState.userId, vehicleId, zoneId, reason, null, amount)
        )
    }
}

class ReportRepository {
    suspend fun summary(): AdminReportSummary = api.getReportSummary().toDomain()
}
