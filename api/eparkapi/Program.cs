using eparkapi.Data;
using eparkapi.Endpoints;
using eparkapi.Models.Enums;
using eparkapi.Services;
using Microsoft.EntityFrameworkCore;
using Npgsql;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddOpenApi();
builder.Services.AddScoped<IEmailSender, SmtpEmailSender>();

const string CorsPolicy = "AllowApp";
builder.Services.AddCors(options =>
    options.AddPolicy(CorsPolicy, policy =>
        policy.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod()));

var connectionString = builder.Configuration.GetConnectionString("Postgres");
var dataSourceBuilder = new NpgsqlDataSourceBuilder(connectionString);
dataSourceBuilder.MapEnum<SessionStatus>("session_status");
dataSourceBuilder.MapEnum<PaymentStatus>("payment_status");
dataSourceBuilder.MapEnum<PaymentReference>("payment_reference");
dataSourceBuilder.MapEnum<FineStatus>("fine_status");
var dataSource = dataSourceBuilder.Build();

builder.Services.AddDbContext<EparkDbContext>(options =>
    options.UseNpgsql(dataSource, npgsql =>
    {
        npgsql.MapEnum<SessionStatus>("session_status");
        npgsql.MapEnum<PaymentStatus>("payment_status");
        npgsql.MapEnum<PaymentReference>("payment_reference");
        npgsql.MapEnum<FineStatus>("fine_status");
    }));

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseCors(CorsPolicy);

app.MapAuthEndpoints();
app.MapZoneEndpoints();
app.MapVehicleEndpoints();
app.MapPaymentMethodEndpoints();
app.MapSessionEndpoints();
app.MapPaymentEndpoints();
app.MapFineEndpoints();
app.MapReportEndpoints();
app.MapAdminLogEndpoints();
app.MapAdminNotificationEndpoints();

app.Run();
