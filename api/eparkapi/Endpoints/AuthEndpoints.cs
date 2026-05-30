using System.Security.Cryptography;
using System.Text;
using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class AuthEndpoints
{
    public static void MapAuthEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/api/auth").WithTags("Auth");

        group.MapPost("/register", async (RegisterRequest req, EparkDbContext db) =>
        {
            if (!IsValidPassword(req.Password))
                return Results.BadRequest(new { message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character." });

            if (await db.Users.AnyAsync(u => u.Email == req.Email))
                return Results.Conflict(new { message = "Email already registered." });

            var driverRole = await db.Roles.FirstOrDefaultAsync(r => r.Name == "driver");
            if (driverRole is null)
                return Results.Problem("Driver role is not seeded.");

            var user = new User
            {
                RoleId = driverRole.Id,
                FullName = req.FullName,
                Email = req.Email,
                PasswordHash = req.Password,
                Phone = req.Phone
            };
            db.Users.Add(user);
            await db.SaveChangesAsync();

            if (!string.IsNullOrWhiteSpace(req.Plate))
            {
                var typeId = req.VehicleTypeId
                    ?? (await db.VehicleTypes.FirstOrDefaultAsync(t => t.Name == "car"))?.Id
                    ?? (short)1;
                db.Vehicles.Add(new Vehicle
                {
                    UserId = user.Id,
                    VehicleTypeId = typeId,
                    Plate = req.Plate!,
                    Brand = req.Brand,
                    Model = req.Model,
                    Color = req.Color
                });
                await db.SaveChangesAsync();
            }

            return Results.Ok(new AuthResponse(user.Id, user.FullName, user.Email, driverRole.Name, null));
        });

        group.MapPost("/login", async (LoginRequest req, EparkDbContext db) =>
        {
            var user = await db.Users
                .Include(u => u.Role)
                .FirstOrDefaultAsync(u => u.Email == req.Email && u.IsActive);

            if (user is null || !user.PasswordHash.Equals(req.Password, StringComparison.Ordinal))
                return Results.Unauthorized();

            return Results.Ok(new AuthResponse(user.Id, user.FullName, user.Email, user.Role.Name, user.MunicipalityId));
        });
    }

    private static string HashPassword(string password)
    {
        var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(password));
        return Convert.ToHexString(bytes);
    }

    private static bool IsValidPassword(string password) =>
        password.Length >= 8 &&
        password.Any(char.IsUpper) &&
        password.Any(char.IsLower) &&
        password.Any(char.IsDigit) &&
        password.Any(c => !char.IsLetterOrDigit(c));
}
