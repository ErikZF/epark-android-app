using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class AdminNotificationEndpoints
{
    public static void MapAdminNotificationEndpoints(this IEndpointRouteBuilder app)
    {
        // Derived live feed for admins: vehicles overdue on an active session, plus
        // fines that have been paid. Computed on each request from existing rows —
        // no notifications table, no background worker. Newest first.
        app.MapGet("/api/admin/notifications", async (EparkDbContext db, int? municipalityId) =>
        {
            var now = DateTime.UtcNow;

            // Project to anonymous types first; enum filtering is fine in SQL but we
            // avoid enum.ToString() inside the query (Npgsql can't translate it).
            var overdue = (await db.Sessions
                .Where(s => s.Status == SessionStatus.Active
                    && s.ScheduledEnd < now
                    && (municipalityId == null || s.Zone.MunicipalityId == municipalityId))
                .Select(s => new
                {
                    s.Id,
                    Plate = s.Vehicle.Plate,
                    ZoneName = s.Zone.Name,
                    s.SpaceNumber,
                    s.ScheduledEnd,
                })
                .ToListAsync())
                .Select(s => new AdminNotificationResponse(
                    $"overdue-{s.Id}", "session_overdue", s.Id,
                    s.Plate, s.ZoneName, s.ScheduledEnd, null, s.SpaceNumber));

            var paidFines = (await db.Fines
                .Where(f => f.Status == FineStatus.Paid
                    && f.PaidAt != null
                    && (municipalityId == null || f.Zone.MunicipalityId == municipalityId))
                .Select(f => new
                {
                    f.Id,
                    Plate = f.Vehicle.Plate,
                    ZoneName = f.Zone.Name,
                    f.Amount,
                    f.PaidAt,
                })
                .ToListAsync())
                .Select(f => new AdminNotificationResponse(
                    $"finepaid-{f.Id}", "fine_paid", f.Id,
                    f.Plate, f.ZoneName, f.PaidAt!.Value, f.Amount, null));

            var notifications = overdue
                .Concat(paidFines)
                .OrderByDescending(n => n.Timestamp)
                .ToList();

            return Results.Ok(notifications);
        }).WithTags("AdminNotifications");
    }
}
