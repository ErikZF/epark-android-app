using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class SessionEndpoints
{
    public static void MapSessionEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapPost("/api/sessions", async (CreateSessionRequest req, EparkDbContext db) =>
        {
            var zone = await db.Zones.FindAsync(req.ZoneId);
            if (zone is null) return Results.NotFound(new { message = "Zone not found." });

            if (req.SpaceNumber < 1 || req.SpaceNumber > zone.TotalSpots)
                return Results.BadRequest(new { message = $"Space must be between 1 and {zone.TotalSpots}." });

            var spaceTaken = await db.Sessions.AnyAsync(s =>
                s.ZoneId == req.ZoneId &&
                s.SpaceNumber == req.SpaceNumber &&
                s.Status == SessionStatus.Active);
            if (spaceTaken)
                return Results.Conflict(new { message = "That space is already occupied." });

            var start = AsUtc(req.ScheduledStart);
            var end = AsUtc(req.ScheduledEnd);
            if (end <= start) return Results.BadRequest(new { message = "End must be after start." });

            var session = new Session
            {
                UserId = req.UserId,
                VehicleId = req.VehicleId,
                ZoneId = req.ZoneId,
                SpaceNumber = req.SpaceNumber,
                ScheduledStart = start,
                ScheduledEnd = end,
                HourlyRate = zone.HourlyRate,
                TotalCost = CostFor(zone.HourlyRate, end - start),
                Status = SessionStatus.Active
            };
            db.Sessions.Add(session);
            await db.SaveChangesAsync();
            return Results.Created($"/api/sessions/{session.Id}", new { session.Id, session.TotalCost });
        }).WithTags("Sessions");

        app.MapGet("/api/users/{userId:int}/sessions", async (int userId, EparkDbContext db) =>
        {
            var sessions = await db.Sessions
                .Where(s => s.UserId == userId)
                .OrderByDescending(s => s.CreatedAt)
                .Select(s => new SessionResponse(
                    s.Id, s.ZoneId, s.Zone.Name, s.VehicleId, s.Vehicle.Plate, s.SpaceNumber,
                    s.ScheduledStart, s.ScheduledEnd, s.ActualEnd,
                    s.HourlyRate, s.TotalCost, s.Status.ToString()))
                .ToListAsync();
            return Results.Ok(sessions);
        }).WithTags("Sessions");

        app.MapGet("/api/users/{userId:int}/sessions/active", async (int userId, EparkDbContext db) =>
        {
            var session = await db.Sessions
                .Where(s => s.UserId == userId && s.Status == SessionStatus.Active)
                .OrderByDescending(s => s.CreatedAt)
                .Select(s => new SessionResponse(
                    s.Id, s.ZoneId, s.Zone.Name, s.VehicleId, s.Vehicle.Plate, s.SpaceNumber,
                    s.ScheduledStart, s.ScheduledEnd, s.ActualEnd,
                    s.HourlyRate, s.TotalCost, s.Status.ToString()))
                .FirstOrDefaultAsync();
            return session is null ? Results.NoContent() : Results.Ok(session);
        }).WithTags("Sessions");

        app.MapPost("/api/sessions/{id:int}/extend", async (int id, ExtendSessionRequest req, EparkDbContext db) =>
        {
            if (req.AddedMinutes <= 0) return Results.BadRequest(new { message = "addedMinutes must be positive." });
            var session = await db.Sessions.FindAsync(id);
            if (session is null) return Results.NotFound();

            var extra = CostFor(session.HourlyRate, TimeSpan.FromMinutes(req.AddedMinutes));
            session.ScheduledEnd = session.ScheduledEnd.AddMinutes(req.AddedMinutes);
            session.TotalCost += extra;
            db.SessionExtensions.Add(new SessionExtension
            {
                SessionId = session.Id,
                AddedMinutes = req.AddedMinutes,
                AdditionalCost = extra
            });
            await db.SaveChangesAsync();
            return Results.Ok(new { session.Id, session.ScheduledEnd, session.TotalCost, additionalCost = extra });
        }).WithTags("Sessions");

        app.MapPost("/api/sessions/{id:int}/finalize", async (int id, EparkDbContext db) =>
        {
            var session = await db.Sessions.FindAsync(id);
            if (session is null) return Results.NotFound();

            session.ActualEnd = DateTime.UtcNow;
            session.Status = SessionStatus.Completed;
            await db.SaveChangesAsync();
            return Results.Ok(new { session.Id, status = session.Status.ToString() });
        }).WithTags("Sessions");
    }

    private static decimal CostFor(decimal hourlyRate, TimeSpan duration) =>
        Math.Round(hourlyRate * (decimal)duration.TotalHours, 2, MidpointRounding.AwayFromZero);

    private static DateTime AsUtc(DateTime value) => value.Kind switch
    {
        DateTimeKind.Utc => value,
        DateTimeKind.Local => value.ToUniversalTime(),
        _ => DateTime.SpecifyKind(value, DateTimeKind.Utc)
    };
}
