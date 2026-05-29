namespace eparkapi.Models.Dtos;

// ── Auth ─────────────────────────────────────────────
public record RegisterRequest(
    string FullName,
    string Email,
    string Password,
    string? Phone,
    string? Plate,
    short? VehicleTypeId,
    string? Brand,
    string? Model,
    string? Color);

public record LoginRequest(string Email, string Password);

public record AuthResponse(int UserId, string FullName, string Email, string Role, int? MunicipalityId);

// ── Zones ────────────────────────────────────────────
public record ZoneResponse(
    int Id,
    int MunicipalityId,
    string MunicipalityName,
    string Name,
    string? Description,
    decimal Latitude,
    decimal Longitude,
    int TotalSpots,
    int FreeSpots,
    decimal HourlyRate,
    bool IsActive);

public record CreateZoneRequest(
    int MunicipalityId,
    string Name,
    string? Description,
    decimal Latitude,
    decimal Longitude,
    int TotalSpots,
    decimal HourlyRate);

public record UpdateZoneRequest(
    string Name,
    string? Description,
    int TotalSpots,
    decimal HourlyRate,
    bool IsActive);

// ── Vehicles ─────────────────────────────────────────
public record VehicleResponse(
    int Id,
    int UserId,
    int VehicleTypeId,
    string VehicleType,
    string Plate,
    string? Nickname,
    string? Brand,
    string? Model,
    string? Color,
    bool IsActive);

public record CreateVehicleRequest(
    int UserId,
    short VehicleTypeId,
    string Plate,
    string? Nickname,
    string? Brand,
    string? Model,
    string? Color);

// ── Payment methods ──────────────────────────────────
public record PaymentMethodResponse(
    int Id,
    string? CardBrand,
    string LastFour,
    short ExpiryMonth,
    short ExpiryYear,
    bool IsDefault);

public record CreatePaymentMethodRequest(
    int UserId,
    string? CardBrand,
    string LastFour,
    short ExpiryMonth,
    short ExpiryYear,
    string Token,
    bool IsDefault);

// ── Sessions ─────────────────────────────────────────
public record CreateSessionRequest(
    int UserId,
    int VehicleId,
    int ZoneId,
    DateTime ScheduledStart,
    DateTime ScheduledEnd);

public record ExtendSessionRequest(int AddedMinutes);

public record SessionResponse(
    int Id,
    int ZoneId,
    string ZoneName,
    int VehicleId,
    string Plate,
    DateTime ScheduledStart,
    DateTime ScheduledEnd,
    DateTime? ActualEnd,
    decimal HourlyRate,
    decimal TotalCost,
    string Status);

// ── Payments ─────────────────────────────────────────
public record CreatePaymentRequest(
    int UserId,
    int? PaymentMethodId,
    decimal Amount,
    string ReferenceType,
    int ReferenceId);

public record PaymentResponse(
    int Id,
    decimal Amount,
    string Currency,
    string Status,
    string ReferenceType,
    int ReferenceId,
    DateTime? PaidAt,
    string? InvoiceNumber);

// ── Fines ────────────────────────────────────────────
public record FineResponse(
    int Id,
    int VehicleId,
    string Plate,
    int ZoneId,
    string ZoneName,
    string Reason,
    string? EvidenceUrl,
    decimal Amount,
    string Status,
    DateTime IssuedAt,
    DateTime? PaidAt);

public record CreateFineRequest(
    int IssuedBy,
    int VehicleId,
    int ZoneId,
    string Reason,
    string? EvidenceUrl,
    decimal Amount);

// ── Reports ──────────────────────────────────────────
public record ReportSummaryResponse(
    int TotalSessions,
    decimal Revenue,
    int FinesIssued,
    int ActiveSpots,
    int TotalSpots);
