namespace eparkapi.Models.Enums;

public enum SessionStatus
{
    Active,
    Completed,
    Cancelled,
    Expired
}

public enum PaymentStatus
{
    Pending,
    Completed,
    Failed,
    Refunded
}

public enum PaymentReference
{
    Session,
    Fine
}

public enum FineStatus
{
    Unpaid,
    Paid,
    Contested,
    Dismissed
}
