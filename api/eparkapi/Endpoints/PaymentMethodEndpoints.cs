using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class PaymentMethodEndpoints
{
    public static void MapPaymentMethodEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/users/{userId:int}/payment-methods", async (int userId, EparkDbContext db) =>
        {
            var methods = await db.PaymentMethods
                .Where(p => p.UserId == userId)
                .Select(p => new PaymentMethodResponse(
                    p.Id, p.CardBrand, p.LastFour, p.ExpiryMonth, p.ExpiryYear, p.IsDefault))
                .ToListAsync();
            return Results.Ok(methods);
        }).WithTags("PaymentMethods");

        app.MapPost("/api/payment-methods", async (CreatePaymentMethodRequest req, EparkDbContext db) =>
        {
            if (req.IsDefault)
            {
                var existing = await db.PaymentMethods.Where(p => p.UserId == req.UserId && p.IsDefault).ToListAsync();
                foreach (var pm in existing) pm.IsDefault = false;
            }

            var method = new PaymentMethod
            {
                UserId = req.UserId,
                CardBrand = req.CardBrand,
                LastFour = req.LastFour,
                ExpiryMonth = req.ExpiryMonth,
                ExpiryYear = req.ExpiryYear,
                Token = req.Token,
                IsDefault = req.IsDefault
            };
            db.PaymentMethods.Add(method);
            await db.SaveChangesAsync();
            return Results.Created($"/api/payment-methods/{method.Id}", new { method.Id });
        }).WithTags("PaymentMethods");
    }
}
