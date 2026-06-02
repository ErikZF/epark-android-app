using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class ReportEndpoints
{
    public static void MapReportEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/admin/reports/summary", async (EparkDbContext db, string? from, string? to, int? municipalityId, int? adminId) =>
        {
            DateTimeOffset? fromDate = null;
            DateTimeOffset? toDate   = null;

            if (!string.IsNullOrWhiteSpace(from) && DateTimeOffset.TryParse(from, out var fd))
                fromDate = fd.ToUniversalTime();
            if (!string.IsNullOrWhiteSpace(to) && DateTimeOffset.TryParse(to, out var td))
                toDate = td.ToUniversalTime().AddDays(1).AddSeconds(-1);

            var sessionsQuery = db.Sessions
                .Where(s => municipalityId == null || s.Zone.MunicipalityId == municipalityId)
                .AsQueryable();
            var finesQuery = db.Fines
                .Where(f => municipalityId == null || f.Zone.MunicipalityId == municipalityId)
                .AsQueryable();

            if (municipalityId.HasValue)
            {
                sessionsQuery = sessionsQuery.Where(s => s.Zone.MunicipalityId == municipalityId.Value);
                finesQuery    = finesQuery.Where(f => f.Zone.MunicipalityId == municipalityId.Value);
            }

            if (fromDate.HasValue)
            {
                sessionsQuery = sessionsQuery.Where(s => s.ScheduledStart >= fromDate.Value);
                finesQuery    = finesQuery.Where(f => f.IssuedAt >= fromDate.Value);
            }
            if (toDate.HasValue)
            {
                sessionsQuery = sessionsQuery.Where(s => s.ScheduledStart <= toDate.Value);
                finesQuery    = finesQuery.Where(f => f.IssuedAt <= toDate.Value);
            }

            var sessionIds = sessionsQuery.Select(s => s.Id);

            var totalSessions = await sessionsQuery.CountAsync();
            var revenue       = await db.Payments
                .Where(p => p.Status == PaymentStatus.Completed
                         && p.ReferenceType == PaymentReference.Session
                         && sessionIds.Contains(p.ReferenceId))
                .SumAsync(p => (decimal?)p.Amount) ?? 0m;
            var finesIssued   = await finesQuery.CountAsync();
            var activeSpots   = await db.Sessions
                .CountAsync(s => s.Status == SessionStatus.Active
                              && (municipalityId == null || s.Zone.MunicipalityId == municipalityId));
            var totalSpots    = await db.Zones
                .Where(z => z.IsActive && (municipalityId == null || z.MunicipalityId == municipalityId))
                .SumAsync(z => (int?)z.TotalSpots) ?? 0;

            // Revenue breakdown per zone (only session payments)
            var revenueByZone = await (
                from p in db.Payments
                join s in sessionsQuery on p.ReferenceId equals s.Id
                join z in db.Zones on s.ZoneId equals z.Id
                where p.Status == PaymentStatus.Completed
                   && p.ReferenceType == PaymentReference.Session
                group p by z.Name into g
                orderby g.Sum(x => x.Amount) descending
                select new ZoneRevenueResponse(g.Key, g.Sum(x => x.Amount), g.Count())
            ).ToListAsync();

            var range = (fromDate, toDate) switch
            {
                ({ }, { }) => $" ({from} a {to})",
                ({ }, null) => $" (desde {from})",
                (null, { }) => $" (hasta {to})",
                _ => "",
            };
            AdminAudit.Log(db, adminId ?? 0, "report.view", $"Consultó el reporte de ingresos{range}");
            await db.SaveChangesAsync();

            return Results.Ok(new ReportSummaryResponse(
                totalSessions, revenue, finesIssued, activeSpots, totalSpots, revenueByZone));
        }).WithTags("Reports");
    }
}
