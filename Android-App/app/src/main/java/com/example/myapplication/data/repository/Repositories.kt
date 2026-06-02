package com.example.myapplication.data.repository

import com.example.myapplication.data.ActiveSession
import com.example.myapplication.data.AdminReportSummary
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.remote.CreateFineRequestDto
import com.example.myapplication.data.remote.CreatePaymentMethodRequestDto
import com.example.myapplication.data.remote.CreatePaymentRequestDto
import com.example.myapplication.data.remote.CreateSessionRequestDto
import com.example.myapplication.data.remote.CreateVehicleRequestDto
import com.example.myapplication.data.remote.CreateZoneRequestDto
import com.example.myapplication.data.remote.EparkApi
import com.example.myapplication.data.remote.UpdateZoneRequestDto
import com.example.myapplication.data.remote.ExtendSessionRequestDto
import com.example.myapplication.data.remote.FinalizeSessionResponseDto
import com.example.myapplication.data.AuthPreferences
import com.example.myapplication.data.remote.LoginRequestDto
import com.example.myapplication.data.remote.RegisterRequestDto

private val api: EparkApi get() = ApiClient.api

class AuthRepository {
    /** Logs in, stores the session in [AuthState], and persists it to DataStore. Returns the role. */
    suspend fun login(email: String, password: String): String {
        val auth = api.login(LoginRequestDto(email.trim(), password))
        AuthState.set(auth)
        AuthPreferences.save(auth)
        return auth.role
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        nationalId: String? = null,
        plate: String? = null,
    ): String {
        val auth = api.register(
            RegisterRequestDto(fullName = fullName, email = email.trim(), password = password, nationalId = nationalId, plate = plate)
        )
        AuthState.set(auth)
        AuthPreferences.save(auth)
        return auth.role
    }
}

class ZoneRepository {
    suspend fun getZones(municipalityId: Int? = null): List<ParkingZone> =
        api.getZones(municipalityId = municipalityId).map { it.toDomain() }

    suspend fun addZone(
        municipalityId: Int,
        name: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        totalSpots: Int,
        hourlyRate: Double,
        openHour: Int,
        closeHour: Int,
    ) = api.createZone(
        CreateZoneRequestDto(
            municipalityId,
            name,
            description,
            latitude,
            longitude,
            totalSpots,
            hourlyRate,
            openHour,
            closeHour,
        )
    )

    suspend fun updateZone(
        id: Int,
        name: String,
        totalSpots: Int,
        hourlyRate: Double,
        openHour: Int,
        closeHour: Int,
        isActive: Boolean,
    ) = api.updateZone(
        id,
        UpdateZoneRequestDto(
            name = name,
            description = null,
            totalSpots = totalSpots,
            hourlyRate = hourlyRate,
            openHour = openHour,
            closeHour = closeHour,
            isActive = isActive,
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

    /** Creates a session and returns the new session ID. */
    suspend fun startSession(vehicleId: Int, zoneId: Int, spaceNumber: Int, startIso: String): Int {
        val response = api.createSession(
            CreateSessionRequestDto(AuthState.userId, vehicleId, zoneId, spaceNumber, startIso)
        )
        return response.id
    }

    /** Returns the active session, or null if none exists (API returns 204). */
    suspend fun getActiveSession(userId: Int = AuthState.userId): ActiveSession? {
        val response = api.getActiveSession(userId)
        return if (response.isSuccessful) response.body()?.toActiveSession() else null
    }

    /** Extends a session. Returns the new scheduledEnd as ISO string and added minutes. */
    suspend fun extend(sessionId: Int, addedMinutes: Int) =
        api.extendSession(sessionId, ExtendSessionRequestDto(addedMinutes))

    /** Finalizes a session and returns the calculated total cost and session ID. */
    suspend fun finalize(sessionId: Int): FinalizeSessionResponseDto =
        api.finalizeSession(sessionId)
}

class PaymentRepository {
    suspend fun getMethods(userId: Int = AuthState.userId): List<PaymentMethod> =
        api.getPaymentMethods(userId).map { it.toDomain() }

    suspend fun addMethod(
        cardBrand: String?,
        lastFour: String,
        expiryMonth: Short,
        expiryYear: Short,
        isDefault: Boolean,
    ) = api.createPaymentMethod(
        CreatePaymentMethodRequestDto(
            userId = AuthState.userId,
            cardBrand = cardBrand,
            lastFour = lastFour,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            token = "tok_sim_${lastFour}",
            isDefault = isDefault,
        )
    )

    suspend fun pay(amount: Double, referenceType: String, referenceId: Int, paymentMethodId: Int? = null) =
        api.createPayment(
            CreatePaymentRequestDto(AuthState.userId, paymentMethodId, amount, referenceType, referenceId)
        )
}

class FineRepository {
    suspend fun getUserFines(userId: Int = AuthState.userId): List<Fine> =
        api.getUserFines(userId).map { it.toDomain() }

    suspend fun getAllFines(municipalityId: Int? = null): List<Fine> = api.getAllFines(municipalityId).map { it.toDomain() }

    suspend fun issueFine(vehicleId: Int, zoneId: Int, reason: String, amount: Double) {
        api.createFine(
            CreateFineRequestDto(AuthState.userId, vehicleId, zoneId, reason, null, amount)
        )
    }
}

class ReportRepository {
    suspend fun summary(from: String? = null, to: String? = null, municipalityId: Int? = null): AdminReportSummary =
        api.getReportSummary(from, to, municipalityId).toDomain()
}
