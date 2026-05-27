using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class PaymentEndpoints
{
    public static void MapPaymentEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/api/payments", async (CreatePaymentRequest req, EparkDbContext db) =>
        {
            if (!Enum.TryParse<PaymentReference>(req.ReferenceType, ignoreCase: true, out var refType))
                return Results.BadRequest(new { message = "referenceType must be 'session' or 'fine'." });
            if (req.Amount <= 0)
                return Results.BadRequest(new { message = "amount must be positive." });

            var now = DateTime.UtcNow;
            var payment = new Payment
            {
                UserId = req.UserId,
                PaymentMethodId = req.PaymentMethodId,
                Amount = req.Amount,
                Status = PaymentStatus.Completed,
                ReferenceType = refType,
                ReferenceId = req.ReferenceId,
                GatewayTransactionId = $"MOCK-{Guid.NewGuid():N}",
                PaidAt = now
            };
            db.Payments.Add(payment);
            await db.SaveChangesAsync();

            var invoice = new Invoice
            {
                PaymentId = payment.Id,
                UserId = req.UserId,
                InvoiceNumber = $"INV-{now:yyyy}-{payment.Id:D6}",
                TotalAmount = req.Amount
            };
            db.Invoices.Add(invoice);

            if (refType == PaymentReference.Fine)
            {
                var fine = await db.Fines.FindAsync(req.ReferenceId);
                if (fine is not null)
                {
                    fine.Status = FineStatus.Paid;
                    fine.PaymentId = payment.Id;
                    fine.PaidAt = now;
                }
            }

            await db.SaveChangesAsync();

            return Results.Ok(new PaymentResponse(
                payment.Id, payment.Amount, payment.Currency, payment.Status.ToString(),
                refType.ToString().ToLowerInvariant(), payment.ReferenceId, payment.PaidAt, invoice.InvoiceNumber));
        }).WithTags("Payments");
    }
}
