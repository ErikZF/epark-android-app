using System.Linq.Expressions;
using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class FineEndpoints
{
    private static readonly Expression<Func<Fine, FineResponse>> Project =
        f => new FineResponse(
            f.Id, f.VehicleId, f.Vehicle.Plate, f.ZoneId, f.Zone.Name,
            f.Reason, f.EvidenceUrl, f.Amount, f.Status.ToString(), f.IssuedAt, f.PaidAt);

    public static void MapFineEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/users/{userId:int}/fines", async (int userId, EparkDbContext db) =>
        {
            var fines = await db.Fines
                .Where(f => f.Vehicle.UserId == userId)
                .OrderByDescending(f => f.IssuedAt)
                .Select(Project)
                .ToListAsync();
            return Results.Ok(fines);
        }).WithTags("Fines");

        app.MapGet("/api/fines", async (EparkDbContext db) =>
        {
            var fines = await db.Fines
                .OrderByDescending(f => f.IssuedAt)
                .Select(Project)
                .ToListAsync();
            return Results.Ok(fines);
        }).WithTags("Fines");

        app.MapPost("/api/fines", async (CreateFineRequest req, EparkDbContext db) =>
        {
            if (req.Amount <= 0) return Results.BadRequest(new { message = "amount must be positive." });

            var fine = new Fine
            {
                IssuedBy = req.IssuedBy,
                VehicleId = req.VehicleId,
                ZoneId = req.ZoneId,
                Reason = req.Reason,
                EvidenceUrl = req.EvidenceUrl,
                Amount = req.Amount
            };
            db.Fines.Add(fine);
            await db.SaveChangesAsync();
            return Results.Created($"/api/fines/{fine.Id}", new { fine.Id });
        }).WithTags("Fines");
    }
}
