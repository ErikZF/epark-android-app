# Configuración de correo de verificación (SMTP)

El registro de conductores envía un **correo de verificación con enlace de
activación** (requerimiento 6.2). El sistema funciona de dos formas:

| Modo | Cuándo | Qué pasa |
|------|--------|----------|
| **Simulado** (por defecto) | No hay SMTP configurado | El enlace de activación se imprime en la **consola del API** |
| **Real** | SMTP configurado por usuario | Se envía un correo real vía Gmail |

> Las credenciales se guardan en **User Secrets**, que viven **fuera del repo**
> (`%APPDATA%\Microsoft\UserSecrets\...`). **Nunca se suben a git.** Por eso cada
> integrante configura las suyas; no se comparten.

---

## Escenario 1 — Solo probar la app (sin configurar nada)

```bash
cd api/eparkapi
dotnet run
```

Al registrar un conductor, busca en la consola del API una línea como:

```
[EMAIL:SIMULADO] Para juan@gmail.com — enlace de activación: http://localhost:5114/api/auth/verify?token=ABC123...
```

Copia ese enlace, ábrelo en el navegador → la cuenta queda activada.
El flujo completo (registro → activar → login) funciona sin Gmail.

---

## Escenario 2 — Enviar correos reales (opcional)

### Paso 1 — Generar un App Password de Gmail

1. Activa la **verificación en 2 pasos** en tu cuenta:
   <https://myaccount.google.com/signinoptions/twosv>
2. Genera un App Password (nombre: `epark`):
   <https://myaccount.google.com/apppasswords>
3. Google te da **16 letras** en 4 grupos. Cópialas **sin espacios**.

> ⚠️ El App Password **no** es tu contraseña normal ni una API key de Google
> Cloud. Es una clave específica de 16 letras minúsculas.

### Paso 2 — Guardar las credenciales (elige una opción)

**Opción A — Visual Studio** (integrado, sin extensión)

1. Clic derecho sobre el proyecto `eparkapi` en el Solution Explorer
2. **"Manage User Secrets"**
3. Pega este JSON (reemplaza con tus datos) y guarda:

```json
{
  "Smtp:Host": "smtp.gmail.com",
  "Smtp:Port": "587",
  "Smtp:Username": "TU_CORREO@gmail.com",
  "Smtp:Password": "TU_APP_PASSWORD_SIN_ESPACIOS",
  "Smtp:From": "TU_CORREO@gmail.com"
}
```

**Opción B — Terminal** (VS Code / Rider / consola)

```bash
cd api/eparkapi
dotnet user-secrets set "Smtp:Host" "smtp.gmail.com"
dotnet user-secrets set "Smtp:Port" "587"
dotnet user-secrets set "Smtp:Username" "TU_CORREO@gmail.com"
dotnet user-secrets set "Smtp:Password" "TU_APP_PASSWORD_SIN_ESPACIOS"
dotnet user-secrets set "Smtp:From" "TU_CORREO@gmail.com"
```

### Paso 3 — Reiniciar el API

```bash
dotnet run
```

Al registrar un conductor, el correo llega de verdad a la bandeja de entrada.
En la consola verás: `Correo de verificación enviado a ...`.

---

## Verificación rápida del App Password (opcional)

Para confirmar que Gmail acepta tu clave antes de tocar el API:

```bash
python -c "import smtplib; s=smtplib.SMTP('smtp.gmail.com',587); s.starttls(); s.login('TU_CORREO@gmail.com','TU_APP_PASSWORD'); print('OK'); s.quit()"
```

- `OK` → la clave funciona.
- `535 ... BadCredentials` → revisa que el correo y el App Password coincidan
  y que la verificación en 2 pasos esté activada.

---

## Notas

- Si un correo falla al enviarse, el API **no rompe el registro**: registra el
  error y el enlace de activación en la consola como respaldo.
- Los usuarios de prueba existentes (seed) ya están marcados como verificados,
  así que pueden iniciar sesión sin pasar por el correo.
