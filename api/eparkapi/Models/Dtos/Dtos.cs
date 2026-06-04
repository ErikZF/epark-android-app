namespace eparkapi.Models.Dtos;

// ── Auth ─────────────────────────────────────────────
public record RegisterRequest(
    string FullName,
    string Email,
    string Password,
    string? NationalId,
    string? Phone,
    string? Plate,
    short? VehicleTypeId,
    string? Brand,
    string? Model,
    string? Color);

public record LoginRequest(string Email, string Password);

public record ResendVerificationRequest(string Email);

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
    int OpenHour,
    int CloseHour,
    bool IsActive);

public record CreateZoneRequest(
    int MunicipalityId,
    string Name,
    string? Description,
    decimal Latitude,
    decimal Longitude,
    int TotalSpots,
    decimal HourlyRate,
    int OpenHour = 6,
    int CloseHour = 22);

public record UpdateZoneRequest(
    string Name,
    string? Description,
    int TotalSpots,
    decimal HourlyRate,
    int OpenHour,
    int CloseHour,
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
    int SpaceNumber,
    DateTime ScheduledStart);

public record ExtendSessionRequest(int AddedMinutes);

public record SessionResponse(
    int Id,
    int ZoneId,
    string ZoneName,
    int VehicleId,
    string Plate,
    int SpaceNumber,
    DateTime ScheduledStart,
    DateTime ScheduledEnd,
    DateTime? ActualEnd,
    decimal HourlyRate,
    decimal TotalCost,
    string Status,
    int ZoneOpenHour,
    int ZoneCloseHour);

public record FinalizeSessionResponse(int Id, string Status, decimal TotalCost);

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
    int SpaceNumber,
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
    int SpaceNumber,
    string Reason,
    string? EvidenceUrl,
    decimal Amount);

// ── Reports ──────────────────────────────────────────
public record ZoneRevenueResponse(string ZoneName, decimal Revenue, int Sessions);

public record ReportSummaryResponse(
    int TotalSessions,
    decimal Revenue,
    int FinesIssued,
    int ActiveSpots,
    int TotalSpots,
    IEnumerable<ZoneRevenueResponse> RevenueByZone);

// ── Admin notifications ──────────────────────────────
// Derived live feed (no table): overdue active sessions + recently paid fines.
public record AdminNotificationResponse(
    string Id,
    string Type,
    int ReferenceId,
    string Plate,
    string ZoneName,
    DateTime Timestamp,
    decimal? Amount,
    int? SpaceNumber);

// ── Admin action log ─────────────────────────────────
public record AdminActionLogResponse(
    long Id,
    int AdminId,
    string AdminName,
    string Action,
    string? Details,
    DateTime CreatedAt);
