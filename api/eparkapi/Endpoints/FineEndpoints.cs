using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class FineEndpoints
{
    // Status.ToString() cannot be translated to SQL with Npgsql PostgreSQL enums,
    // so we project to an anonymous type first, then convert in memory.
    private static async Task<List<FineResponse>> ProjectAsync(IQueryable<Fine> query) =>
        (await query
            .OrderByDescending(f => f.IssuedAt)
            .Select(f => new {
                f.Id, f.VehicleId, VehiclePlate = f.Vehicle.Plate, f.ZoneId,
                ZoneName = f.Zone.Name, f.Reason, f.EvidenceUrl, f.Amount,
                f.Status, f.IssuedAt, f.PaidAt,
            })
            .ToListAsync())
        .Select(f => new FineResponse(
            f.Id, f.VehicleId, f.VehiclePlate, f.ZoneId, f.ZoneName,
            f.Reason, f.EvidenceUrl, f.Amount, f.Status.ToString(), f.IssuedAt, f.PaidAt))
        .ToList();

    public static void MapFineEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/users/{userId:int}/fines", async (int userId, EparkDbContext db) =>
        {
            var fines = await ProjectAsync(db.Fines.Where(f => f.Vehicle.UserId == userId));
            return Results.Ok(fines);
        }).WithTags("Fines");

        app.MapGet("/api/fines", async (EparkDbContext db) =>
        {
            var fines = await ProjectAsync(db.Fines);
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
