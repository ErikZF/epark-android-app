package com.example.myapplication.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recibe la alarma programada por [SessionAlarmScheduler] y muestra la notificación
 * de "sesión por vencer". Se ejecuta fuera del ciclo de vida de la app: cuando dispara,
 * el sistema puede recrear el proceso desde cero, por lo que NO podemos asumir que
 * [AlertPreferences] esté inicializado ni que los canales existan. Todo lo necesario
 * llega por extras y los canales se recrean defensivamente (crear uno existente es no-op).
 */
class SessionExpiryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val minutesLeft = intent.getIntExtra(EXTRA_MINUTES_LEFT, 10)
        val zoneName = intent.getStringExtra(EXTRA_ZONE_NAME)

        NotificationHelper.createChannels(context)
        NotificationHelper.showSessionExpiry(context, minutesLeft, zoneName)
    }

    companion object {
        const val ACTION_SESSION_EXPIRY = "com.example.myapplication.SESSION_EXPIRY"
        const val EXTRA_MINUTES_LEFT = "minutes_left"
        const val EXTRA_ZONE_NAME = "zone_name"
    }
}
