using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Enums;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class ReportEndpoints
{
    public static void MapReportEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/admin/reports/summary", async (EparkDbContext db) =>
        {
            var totalSessions = await db.Sessions.CountAsync();
            var revenue = await db.Payments
                .Where(p => p.Status == PaymentStatus.Completed)
                .SumAsync(p => (decimal?)p.Amount) ?? 0m;
            var finesIssued = await db.Fines.CountAsync();
            var activeSpots = await db.Sessions.CountAsync(s => s.Status == SessionStatus.Active);
            var totalSpots = await db.Zones.Where(z => z.IsActive).SumAsync(z => (int?)z.TotalSpots) ?? 0;

            return Results.Ok(new ReportSummaryResponse(
                totalSessions, revenue, finesIssued, activeSpots, totalSpots));
        }).WithTags("Reports");
    }
}
