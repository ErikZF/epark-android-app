using eparkapi.Models.Enums;

namespace eparkapi.Models.Entities;

public class Role
{
    public short Id { get; set; }
    public string Name { get; set; } = null!;
}

public class Municipality
{
    public int Id { get; set; }
    public string Name { get; set; } = null!;
    public string Country { get; set; } = "Costa Rica";
    public DateTime CreatedAt { get; set; }
}

public class User
{
    public int Id { get; set; }
    public short RoleId { get; set; }
    public int? MunicipalityId { get; set; }
    public string FullName { get; set; } = null!;
    public string Email { get; set; } = null!;
    public string PasswordHash { get; set; } = null!;
    public string? NationalId { get; set; }
    public string? Phone { get; set; }
    public string? AvatarUrl { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public Role Role { get; set; } = null!;
    public Municipality? Municipality { get; set; }
}

public class Zone
{
    public int Id { get; set; }
    public int MunicipalityId { get; set; }
    public string Name { get; set; } = null!;
    public string? Description { get; set; }
    public decimal Latitude { get; set; }
    public decimal Longitude { get; set; }
    public int TotalSpots { get; set; }
    public decimal HourlyRate { get; set; }
    // Local (Costa Rica, UTC-6) hour when zone opens (0-23)
    public int OpenHour { get; set; } = 6;
    // Local (Costa Rica, UTC-6) hour when zone closes (1-24; 24 = midnight of next day)
    public int CloseHour { get; set; } = 22;
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public Municipality Municipality { get; set; } = null!;
}

public class VehicleType
{
    public short Id { get; set; }
    public string Name { get; set; } = null!;
}

public class Vehicle
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public short VehicleTypeId { get; set; }
    public string Plate { get; set; } = null!;
    public string? Nickname { get; set; }
    public string? Brand { get; set; }
    public string? Model { get; set; }
    public string? Color { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedAt { get; set; }

    public VehicleType VehicleType { get; set; } = null!;
}

public class PaymentMethod
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public string? CardBrand { get; set; }
    public string LastFour { get; set; } = null!;
    public short ExpiryMonth { get; set; }
    public short ExpiryYear { get; set; }
    public string Token { get; set; } = null!;
    public bool IsDefault { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class Session
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public int VehicleId { get; set; }
    public int ZoneId { get; set; }
    public int SpaceNumber { get; set; }
    public DateTime ScheduledStart { get; set; }
    public DateTime ScheduledEnd { get; set; }
    public DateTime? ActualEnd { get; set; }
    public decimal HourlyRate { get; set; }
    public decimal TotalCost { get; set; }
    public SessionStatus Status { get; set; } = SessionStatus.Active;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public Zone Zone { get; set; } = null!;
    public Vehicle Vehicle { get; set; } = null!;
}

public class SessionExtension
{
    public int Id { get; set; }
    public int SessionId { get; set; }
    public int AddedMinutes { get; set; }
    public decimal AdditionalCost { get; set; }
    public DateTime ExtendedAt { get; set; }
}

public class Payment
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public int? PaymentMethodId { get; set; }
    public decimal Amount { get; set; }
    public string Currency { get; set; } = "CRC";
    public PaymentStatus Status { get; set; } = PaymentStatus.Pending;
    public PaymentReference ReferenceType { get; set; }
    public int ReferenceId { get; set; }
    public string? GatewayTransactionId { get; set; }
    public DateTime? PaidAt { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class Invoice
{
    public int Id { get; set; }
    public int PaymentId { get; set; }
    public int UserId { get; set; }
    public string InvoiceNumber { get; set; } = null!;
    public decimal TotalAmount { get; set; }
    public string? PdfUrl { get; set; }
    public DateTime IssuedAt { get; set; }
}

public class Fine
{
    public int Id { get; set; }
    public int IssuedBy { get; set; }
    public int VehicleId { get; set; }
    public int ZoneId { get; set; }
    public string Reason { get; set; } = null!;
    public string? EvidenceUrl { get; set; }
    public decimal Amount { get; set; }
    public FineStatus Status { get; set; } = FineStatus.Unpaid;
    public int? PaymentId { get; set; }
    public DateTime IssuedAt { get; set; }
    public DateTime? PaidAt { get; set; }

    public Vehicle Vehicle { get; set; } = null!;
    public Zone Zone { get; set; } = null!;
}
