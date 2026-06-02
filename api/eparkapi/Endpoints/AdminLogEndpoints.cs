using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class AdminLogEndpoints
{
    public static void MapAdminLogEndpoints(this IEndpointRouteBuilder app)
    {
        // Audit trail of admin actions, newest first. Visible to every admin so
        // nobody can quietly abuse power.
        app.MapGet("/api/admin/logs", async (EparkDbContext db, int? limit) =>
        {
            var take = Math.Clamp(limit ?? 100, 1, 500);
            var logs = await db.AdminActionLogs
                .OrderByDescending(l => l.CreatedAt)
                .Take(take)
                .Select(l => new AdminActionLogResponse(
                    l.Id, l.AdminId, l.Admin.FullName, l.Action, l.Details, l.CreatedAt))
                .ToListAsync();
            return Results.Ok(logs);
        }).WithTags("AdminLogs");
    }
}

/// <summary>
/// Records privileged admin actions into the audit trail. The entry is staged on
/// the context; the caller's SaveChanges persists it alongside the action itself.
/// A non-positive adminId is ignored (no authenticated admin to attribute).
/// </summary>
public static class AdminAudit
{
    public static void Log(EparkDbContext db, int adminId, string action, string? details = null)
    {
        if (adminId <= 0) return;
        db.AdminActionLogs.Add(new AdminActionLog
        {
            AdminId = adminId,
            Action = action,
            Details = details,
        });
    }
}
