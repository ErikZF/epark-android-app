using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class VehicleEndpoints
{
    public static void MapVehicleEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/api/users/{userId:int}/vehicles", async (int userId, EparkDbContext db) =>
        {
            var vehicles = await db.Vehicles
                .Where(v => v.UserId == userId && v.IsActive)
                .Include(v => v.VehicleType)
                .Select(v => new VehicleResponse(
                    v.Id, v.UserId, v.VehicleTypeId, v.VehicleType.Name,
                    v.Plate, v.Nickname, v.Brand, v.Model, v.Color, v.IsActive))
                .ToListAsync();
            return Results.Ok(vehicles);
        }).WithTags("Vehicles");

        app.MapPost("/api/vehicles", async (CreateVehicleRequest req, EparkDbContext db) =>
        {
            if (await db.Vehicles.AnyAsync(v => v.Plate == req.Plate))
                return Results.Conflict(new { message = "Plate already registered." });

            var vehicle = new Vehicle
            {
                UserId = req.UserId,
                VehicleTypeId = req.VehicleTypeId,
                Plate = req.Plate,
                Nickname = req.Nickname,
                Brand = req.Brand,
                Model = req.Model,
                Color = req.Color
            };
            db.Vehicles.Add(vehicle);
            await db.SaveChangesAsync();
            return Results.Created($"/api/vehicles/{vehicle.Id}", new { vehicle.Id });
        }).WithTags("Vehicles");
    }
}
