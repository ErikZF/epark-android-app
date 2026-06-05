package com.example.myapplication.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EparkApi {

    // ── Auth ──
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body body: ResendVerificationRequestDto): Response<Unit>

    @GET("api/auth/check-verification")
    suspend fun checkVerification(@Query("email") email: String): CheckVerificationResponseDto

    // ── Zones ──
    @GET("api/zones")
    suspend fun getZones(
        @Query("includeInactive") includeInactive: Boolean = false,
        @Query("municipalityId") municipalityId: Int? = null,
    ): List<ZoneDto>

    @GET("api/zones/{id}")
    suspend fun getZone(@Path("id") id: Int): ZoneDto

    @POST("api/zones")
    suspend fun createZone(@Body body: CreateZoneRequestDto, @Query("adminId") adminId: Int)

    @PUT("api/zones/{id}")
    suspend fun updateZone(@Path("id") id: Int, @Body body: UpdateZoneRequestDto, @Query("adminId") adminId: Int)

    // ── Vehicles ──
    @GET("api/users/{userId}/vehicles")
    suspend fun getVehicles(@Path("userId") userId: Int): List<VehicleDto>

    @GET("api/vehicles/by-plate/{plate}")
    suspend fun getVehicleByPlate(@Path("plate") plate: String): VehicleDto

    @POST("api/vehicles")
    suspend fun createVehicle(@Body body: CreateVehicleRequestDto)

    // ── Payment methods ──
    @GET("api/users/{userId}/payment-methods")
    suspend fun getPaymentMethods(@Path("userId") userId: Int): List<PaymentMethodDto>

    @POST("api/payment-methods")
    suspend fun createPaymentMethod(@Body body: CreatePaymentMethodRequestDto)

    // ── Sessions ──
    @POST("api/sessions")
    suspend fun createSession(@Body body: CreateSessionRequestDto): CreateSessionResponseDto

    @GET("api/users/{userId}/sessions")
    suspend fun getSessions(@Path("userId") userId: Int): List<SessionDto>

    // Returns 200 with session or 204 No Content when none active
    @GET("api/users/{userId}/sessions/active")
    suspend fun getActiveSession(@Path("userId") userId: Int): Response<SessionDto>

    @POST("api/sessions/{id}/extend")
    suspend fun extendSession(@Path("id") id: Int, @Body body: ExtendSessionRequestDto): ExtendSessionResponseDto

    @POST("api/sessions/{id}/finalize")
    suspend fun finalizeSession(@Path("id") id: Int): FinalizeSessionResponseDto

    // ── Payments ──
    @POST("api/payments")
    suspend fun createPayment(@Body body: CreatePaymentRequestDto): PaymentDto

    // ── Fines ──
    @GET("api/users/{userId}/fines")
    suspend fun getUserFines(@Path("userId") userId: Int): List<FineDto>

    @GET("api/fines")
    suspend fun getAllFines(@Query("municipalityId") municipalityId: Int? = null): List<FineDto>

    @POST("api/fines")
    suspend fun createFine(@Body body: CreateFineRequestDto)

    // ── Reports ──
    @GET("api/admin/reports/summary")
    suspend fun getReportSummary(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("municipalityId") municipalityId: Int? = null,
        @Query("adminId") adminId: Int? = null,
    ): ReportSummaryDto

    // ── Admin action log ──
    @GET("api/admin/logs")
    suspend fun getAdminLogs(@Query("limit") limit: Int? = null): List<AdminActionLogDto>

    // ── Admin notifications ──
    @GET("api/admin/notifications")
    suspend fun getAdminNotifications(@Query("municipalityId") municipalityId: Int? = null): List<AdminNotificationDto>
}
