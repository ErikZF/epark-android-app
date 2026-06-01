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

            // Compare zone hours (local Costa Rica time) against the local equivalent of start
            var startLocal = TimeZoneInfo.ConvertTimeFromUtc(start, LocalTz);
            var openTimeLocal  = startLocal.Date.AddHours(zone.OpenHour);
            var closeTimeLocal = startLocal.Date.AddHours(zone.CloseHour);

            if (startLocal < openTimeLocal)
                return Results.BadRequest(new { message = "Zone is not yet open." });
            if (startLocal >= closeTimeLocal)
                return Results.BadRequest(new { message = "Zone is already closed for today." });

            // scheduledEnd = start + 1 hour, capped at the zone's closing time
            var closeTime   = TimeZoneInfo.ConvertTimeToUtc(closeTimeLocal, LocalTz);
            var defaultEnd  = start.AddHours(1);
            var scheduledEnd = defaultEnd < closeTime ? defaultEnd : closeTime;

            var session = new Session
            {
                UserId = req.UserId,
                VehicleId = req.VehicleId,
                ZoneId = req.ZoneId,
                SpaceNumber = req.SpaceNumber,
                ScheduledStart = start,
                ScheduledEnd = scheduledEnd,
                HourlyRate = zone.HourlyRate,
                TotalCost = CostFor(zone.HourlyRate, scheduledEnd - start),
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
                    s.HourlyRate, s.TotalCost, s.Status.ToString(),
                    s.Zone.OpenHour, s.Zone.CloseHour))
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
                    s.HourlyRate, s.TotalCost, s.Status.ToString(),
                    s.Zone.OpenHour, s.Zone.CloseHour))
                .FirstOrDefaultAsync();
            return session is null ? Results.NoContent() : Results.Ok(session);
        }).WithTags("Sessions");

        app.MapPost("/api/sessions/{id:int}/extend", async (int id, ExtendSessionRequest req, EparkDbContext db) =>
        {
            if (req.AddedMinutes <= 0) return Results.BadRequest(new { message = "addedMinutes must be positive." });

            var session = await db.Sessions.Include(s => s.Zone).FirstOrDefaultAsync(s => s.Id == id);
            if (session is null) return Results.NotFound();
            if (session.Status != SessionStatus.Active)
                return Results.BadRequest(new { message = "Session is not active." });

            // Clamp added minutes so scheduledEnd never exceeds zone closing time
            var zoneClose = ZoneCloseUtc(session.ScheduledEnd, session.Zone.CloseHour);
            var availableMinutes = (int)(zoneClose - session.ScheduledEnd).TotalMinutes;
            if (availableMinutes <= 0)
                return Results.BadRequest(new { message = "Zone is already at its closing time. Cannot extend." });

            var clampedMinutes = Math.Min(req.AddedMinutes, availableMinutes);

            var extra = CostFor(session.HourlyRate, TimeSpan.FromMinutes(clampedMinutes));
            session.ScheduledEnd = session.ScheduledEnd.AddMinutes(clampedMinutes);
            session.TotalCost += extra;
            db.SessionExtensions.Add(new SessionExtension
            {
                SessionId = session.Id,
                AddedMinutes = clampedMinutes,
                AdditionalCost = extra
            });
            await db.SaveChangesAsync();
            return Results.Ok(new
            {
                session.Id,
                session.ScheduledEnd,
                session.TotalCost,
                additionalCost = extra,
                addedMinutes = clampedMinutes,
            });
        }).WithTags("Sessions");

        app.MapPost("/api/sessions/{id:int}/finalize", async (int id, EparkDbContext db) =>
        {
            var session = await db.Sessions.FindAsync(id);
            if (session is null) return Results.NotFound();
            if (session.Status != SessionStatus.Active)
                return Results.BadRequest(new { message = "Session is not active." });

            session.ActualEnd = DateTime.UtcNow;
            // Bill for actual time used, capped at scheduledEnd
            var billableEnd = session.ActualEnd.Value < session.ScheduledEnd
                ? session.ActualEnd.Value
                : session.ScheduledEnd;
            session.TotalCost = CostFor(session.HourlyRate, billableEnd - session.ScheduledStart);
            session.Status = SessionStatus.Completed;
            await db.SaveChangesAsync();
            return Results.Ok(new FinalizeSessionResponse(session.Id, session.Status.ToString(), session.TotalCost));
        }).WithTags("Sessions");
    }

    private static decimal CostFor(decimal hourlyRate, TimeSpan duration) =>
        Math.Round(hourlyRate * (decimal)duration.TotalHours, 2, MidpointRounding.AwayFromZero);

    // Costa Rica is UTC-6 with no DST. Using a fixed offset avoids platform timezone database issues.
    private static readonly TimeZoneInfo LocalTz =
        TimeZoneInfo.CreateCustomTimeZone("CR", TimeSpan.FromHours(-6), "Costa Rica", "Costa Rica");

    // Zone close UTC timestamp. closeHour is in local (Costa Rica) time.
    private static DateTime ZoneCloseUtc(DateTime referenceUtc, int closeHour)
    {
        var localRef = TimeZoneInfo.ConvertTimeFromUtc(referenceUtc, LocalTz);
        var localClose = localRef.Date.AddHours(closeHour);
        return TimeZoneInfo.ConvertTimeToUtc(localClose, LocalTz);
    }

    private static DateTime AsUtc(DateTime value) => value.Kind switch
    {
        DateTimeKind.Utc => value,
        DateTimeKind.Local => value.ToUniversalTime(),
        _ => DateTime.SpecifyKind(value, DateTimeKind.Utc)
    };
}
