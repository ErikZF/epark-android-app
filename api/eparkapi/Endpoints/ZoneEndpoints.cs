using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class ZoneEndpoints
{
    public static void MapZoneEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/api/zones").WithTags("Zones");

        group.MapGet("/", async (EparkDbContext db, bool includeInactive = false) =>
        {
            var rows = await db.Zones
                .Where(z => includeInactive || z.IsActive)
                .Select(z => new
                {
                    Zone = z,
                    Municipality = z.Municipality.Name,
                    Active = db.Sessions.Count(s => s.ZoneId == z.Id && s.Status == SessionStatus.Active)
                })
                .ToListAsync();

            var result = rows.Select(r => ToResponse(r.Zone, r.Municipality, r.Active));
            return Results.Ok(result);
        });

        group.MapGet("/{id:int}", async (int id, EparkDbContext db) =>
        {
            var row = await db.Zones
                .Where(z => z.Id == id)
                .Select(z => new
                {
                    Zone = z,
                    Municipality = z.Municipality.Name,
                    Active = db.Sessions.Count(s => s.ZoneId == z.Id && s.Status == SessionStatus.Active)
                })
                .FirstOrDefaultAsync();

            return row is null
                ? Results.NotFound()
                : Results.Ok(ToResponse(row.Zone, row.Municipality, row.Active));
        });

        group.MapPost("/", async (CreateZoneRequest req, EparkDbContext db) =>
        {
            var zone = new Zone
            {
                MunicipalityId = req.MunicipalityId,
                Name = req.Name,
                Description = req.Description,
                Latitude = req.Latitude,
                Longitude = req.Longitude,
                TotalSpots = req.TotalSpots,
                HourlyRate = req.HourlyRate
            };
            db.Zones.Add(zone);
            await db.SaveChangesAsync();
            return Results.Created($"/api/zones/{zone.Id}", new { zone.Id });
        });

        group.MapPut("/{id:int}", async (int id, UpdateZoneRequest req, EparkDbContext db) =>
        {
            var zone = await db.Zones.FindAsync(id);
            if (zone is null) return Results.NotFound();

            zone.Name = req.Name;
            zone.Description = req.Description;
            zone.TotalSpots = req.TotalSpots;
            zone.HourlyRate = req.HourlyRate;
            zone.IsActive = req.IsActive;
            await db.SaveChangesAsync();
            return Results.NoContent();
        });
    }

    private static ZoneResponse ToResponse(Zone z, string municipality, int activeSessions) =>
        new(
            z.Id,
            z.MunicipalityId,
            municipality,
            z.Name,
            z.Description,
            z.Latitude,
            z.Longitude,
            z.TotalSpots,
            Math.Max(0, z.TotalSpots - activeSessions),
            z.HourlyRate,
            z.IsActive);
}
