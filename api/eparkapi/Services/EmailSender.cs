using System.Net;
using System.Net.Mail;

namespace eparkapi.Services;

public interface IEmailSender
{
    Task SendVerificationEmailAsync(string toEmail, string toName, string activationUrl);
}

/// <summary>
/// Sends the account-activation email (requirement 6.2).
///
/// When an SMTP host is configured under the "Smtp" section it sends a real
/// email; otherwise it falls back to logging the activation link to the
/// console so the flow stays fully testable without SMTP credentials.
/// </summary>
public class SmtpEmailSender(IConfiguration config, ILogger<SmtpEmailSender> logger) : IEmailSender
{
    public async Task SendVerificationEmailAsync(string toEmail, string toName, string activationUrl)
    {
        var host = config["Smtp:Host"];
        var fromAddress = config["Smtp:From"] ?? config["Smtp:Username"];

        // No SMTP configured → dev fallback: log the link so it can still be used.
        if (string.IsNullOrWhiteSpace(host) || string.IsNullOrWhiteSpace(fromAddress))
        {
            logger.LogWarning(
                "[EMAIL:SIMULADO] Para {Email} — enlace de activación: {Url}",
                toEmail, activationUrl);
            return;
        }

        var port = int.TryParse(config["Smtp:Port"], out var p) ? p : 587;
        var username = config["Smtp:Username"];
        var password = config["Smtp:Password"];

        using var client = new SmtpClient(host, port)
        {
            EnableSsl = true,
            DeliveryMethod = SmtpDeliveryMethod.Network,
            // Must be set to false BEFORE assigning Credentials, otherwise
            // SmtpClient ignores the explicit credentials and Gmail rejects auth.
            UseDefaultCredentials = false,
            Credentials = new NetworkCredential(username, password),
        };

        var message = new MailMessage
        {
            From = new MailAddress(fromAddress, "e-park"),
            Subject = "Activa tu cuenta e-park",
            IsBodyHtml = true,
            Body = BuildHtmlBody(toName, activationUrl),
        };
        message.To.Add(new MailAddress(toEmail, toName));

        try
        {
            await client.SendMailAsync(message);
            logger.LogInformation("Correo de verificación enviado a {Email}.", toEmail);
        }
        catch (Exception ex)
        {
            // Never let a mail failure break registration; log the link as fallback.
            logger.LogError(ex,
                "Fallo al enviar correo a {Email}. Enlace de activación: {Url}",
                toEmail, activationUrl);
        }
    }

    private static string BuildHtmlBody(string name, string url) => $$"""
        <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto">
          <h2 style="color:#1f9d6b">Bienvenido a e-park, {{name}}</h2>
          <p>Gracias por registrarte. Para activar tu cuenta y comenzar a usar
             el parqueo inteligente, haz clic en el siguiente botón:</p>
          <p style="text-align:center;margin:28px 0">
            <a href="{{url}}"
               style="background:#1f9d6b;color:#fff;text-decoration:none;
                      padding:12px 28px;border-radius:8px;font-weight:bold">
              Activar mi cuenta
            </a>
          </p>
          <p style="font-size:12px;color:#666">
            Si el botón no funciona, copia y pega este enlace en tu navegador:<br>
            <a href="{{url}}">{{url}}</a>
          </p>
        </div>
        """;
}
