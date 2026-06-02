package com.example.myapplication.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Programa una alarma a nivel de sistema operativo para avisar al conductor cuando
 * falten [alertMinutes] para que venza su sesión de parqueo. A diferencia de la
 * notificación in-app, esta dispara aunque la app esté en segundo plano o cerrada,
 * porque el sistema entrega el broadcast a [SessionExpiryReceiver] de forma autónoma.
 */
object SessionAlarmScheduler {

    // Solo hay una sesión activa a la vez, así que un request code constante basta
    // y permite cancelar la alarma reconstruyendo un PendingIntent equivalente.
    private const val REQUEST_CODE = 4101

    /**
     * Programa (o reprograma) la alarma para `scheduledEndMs - alertMinutes`.
     * Si ese instante ya pasó no hace nada: la sesión ya está por vencer o vencida
     * y la alerta in-app cubre ese caso.
     */
    fun schedule(context: Context, scheduledEndMs: Long, alertMinutes: Int, zoneName: String) {
        val triggerAtMs = scheduledEndMs - alertMinutes * 60_000L
        if (triggerAtMs <= System.currentTimeMillis()) {
            cancel(context)
            return
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = buildPendingIntent(context, alertMinutes, zoneName)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
        } else {
            // Sin permiso de alarmas exactas: inexacta pero permitida durante Doze.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pending)
        }
    }

    /** Cancela cualquier alarma pendiente (p. ej. al pagar/finalizar la sesión antes de tiempo). */
    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, 0, ""))
    }

    private fun buildPendingIntent(context: Context, alertMinutes: Int, zoneName: String): PendingIntent {
        // Los extras no participan en la igualdad de Intents para cancelar; basta con
        // que action + componente + requestCode coincidan.
        val intent = Intent(context, SessionExpiryReceiver::class.java).apply {
            action = SessionExpiryReceiver.ACTION_SESSION_EXPIRY
            putExtra(SessionExpiryReceiver.EXTRA_MINUTES_LEFT, alertMinutes)
            putExtra(SessionExpiryReceiver.EXTRA_ZONE_NAME, zoneName)
        }
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
