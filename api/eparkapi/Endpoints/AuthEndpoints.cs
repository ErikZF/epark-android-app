using System.Security.Cryptography;
using System.Text;
using eparkapi.Data;
using eparkapi.Models.Dtos;
using eparkapi.Models.Entities;
using eparkapi.Services;
using Microsoft.EntityFrameworkCore;

namespace eparkapi.Endpoints;

public static class AuthEndpoints
{
    public static void MapAuthEndpoints(this IEndpointRouteBuilder app)
    {
        var group = app.MapGroup("/api/auth").WithTags("Auth");

        group.MapPost("/register", async (RegisterRequest req, EparkDbContext db, IEmailSender email, IConfiguration config) =>
        {
            if (!IsValidPassword(req.Password))
                return Results.BadRequest(new { message = "Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a special character." });

            if (await db.Users.AnyAsync(u => u.Email == req.Email))
                return Results.Conflict(new { message = "Correo ya registrado." });

            if (!string.IsNullOrWhiteSpace(req.NationalId) && await db.Users.AnyAsync(u => u.NationalId == req.NationalId))
                return Results.Conflict(new { message = "Cédula ya registrada." });

            var driverRole = await db.Roles.FirstOrDefaultAsync(r => r.Name == "driver");
            if (driverRole is null)
                return Results.Problem("Driver role is not seeded.");

            var token = GenerateToken();
            var user = new User
            {
                RoleId = driverRole.Id,
                FullName = req.FullName,
                Email = req.Email,
                PasswordHash = req.Password,
                NationalId = req.NationalId,
                Phone = req.Phone,
                EmailVerified = false,
                VerificationToken = token,
                VerificationSentAt = DateTime.UtcNow,
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

            await email.SendVerificationEmailAsync(user.Email, user.FullName, BuildActivationUrl(config, token));

            return Results.Ok(new AuthResponse(user.Id, user.FullName, user.Email, driverRole.Name, null));
        });

        // Account activation — opened from the link in the verification email.
        group.MapGet("/verify", async (string token, EparkDbContext db) =>
        {
            var user = string.IsNullOrWhiteSpace(token)
                ? null
                : await db.Users.FirstOrDefaultAsync(u => u.VerificationToken == token);

            if (user is null)
                return Results.Content(VerificationPage(false), "text/html");

            user.EmailVerified = true;
            user.VerificationToken = null;
            await db.SaveChangesAsync();

            return Results.Content(VerificationPage(true), "text/html");
        });

        // Re-send the activation email for an unverified account.
        group.MapPost("/resend-verification", async (ResendVerificationRequest req, EparkDbContext db, IEmailSender email, IConfiguration config) =>
        {
            var user = await db.Users.FirstOrDefaultAsync(u => u.Email == req.Email);
            // Don't reveal whether the email exists; always report success.
            if (user is null || user.EmailVerified)
                return Results.Ok(new { message = "Si la cuenta existe y no está verificada, se reenvió el correo." });

            user.VerificationToken = GenerateToken();
            user.VerificationSentAt = DateTime.UtcNow;
            await db.SaveChangesAsync();

            await email.SendVerificationEmailAsync(user.Email, user.FullName, BuildActivationUrl(config, user.VerificationToken!));
            return Results.Ok(new { message = "Si la cuenta existe y no está verificada, se reenvió el correo." });
        });

        // Poll-able endpoint: returns whether the account email has been verified.
        group.MapGet("/check-verification", async (string email, EparkDbContext db) =>
        {
            if (string.IsNullOrWhiteSpace(email))
                return Results.BadRequest();

            var user = await db.Users.FirstOrDefaultAsync(u => u.Email == email && u.IsActive);
            if (user is null)
                return Results.NotFound();

            return Results.Ok(new { verified = user.EmailVerified });
        });

        group.MapPost("/login", async (LoginRequest req, EparkDbContext db) =>
        {
            var user = await db.Users
                .Include(u => u.Role)
                .FirstOrDefaultAsync(u => u.Email == req.Email && u.IsActive);

            if (user is null || !user.PasswordHash.Equals(req.Password, StringComparison.Ordinal))
                return Results.Unauthorized();

            if (!user.EmailVerified)
                return Results.Json(
                    new { message = "Debes verificar tu correo electrónico antes de iniciar sesión." },
                    statusCode: StatusCodes.Status403Forbidden);

            return Results.Ok(new AuthResponse(user.Id, user.FullName, user.Email, user.Role.Name, user.MunicipalityId));
        });
    }

    private static string GenerateToken() =>
        Convert.ToHexString(RandomNumberGenerator.GetBytes(32));

    private static string BuildActivationUrl(IConfiguration config, string token)
    {
        var baseUrl = (config["App:BaseUrl"] ?? "http://localhost:5114").TrimEnd('/');
        return $"{baseUrl}/api/auth/verify?token={token}";
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

    private static string VerificationPage(bool success)
    {
        var (icon, title, body, color) = success
            ? ("✓", "¡Cuenta activada!", "Tu correo fue verificado. Ya puedes iniciar sesión en la app e-park.", "#1f9d6b")
            : ("✕", "Enlace inválido", "El enlace de activación no es válido o ya fue utilizado.", "#d9534f");

        return $$"""
            <!DOCTYPE html>
            <html lang="es"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>e-park</title></head>
            <body style="font-family:Arial,sans-serif;background:#f4f6f5;margin:0;
                         display:flex;align-items:center;justify-content:center;min-height:100vh">
              <div style="background:#fff;border-radius:16px;padding:40px;max-width:380px;
                          text-align:center;box-shadow:0 4px 20px rgba(0,0,0,.08)">
                <div style="width:72px;height:72px;border-radius:50%;background:{{color}};
                            color:#fff;font-size:38px;line-height:72px;margin:0 auto 20px">{{icon}}</div>
                <h2 style="color:{{color}};margin:0 0 12px">{{title}}</h2>
                <p style="color:#555;font-size:15px;margin:0">{{body}}</p>
              </div>
            </body></html>
            """;
    }
}
