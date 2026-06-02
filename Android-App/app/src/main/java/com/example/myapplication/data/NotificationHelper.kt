package com.example.myapplication.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity

object NotificationHelper {

    private const val CHANNEL_SESSION = "epark_session"
    private const val CHANNEL_ADMIN   = "epark_admin"
    private const val ID_SESSION_EXPIRY = 1001
    private const val ID_ADMIN_ALERT    = 1002

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_SESSION, "Sesión de parqueo", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de vencimiento de sesión"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ADMIN, "Alertas administrativas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notificaciones para el administrador municipal"
            }
        )
    }

    fun showSessionExpiry(context: Context, minutesLeft: Int, zoneName: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "active_session")
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val zoneSuffix = if (!zoneName.isNullOrBlank()) " en $zoneName" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏱ Sesión por vencer")
            .setContentText("Tu parqueo$zoneSuffix vence en $minutesLeft minuto${if (minutesLeft != 1) "s" else ""}. Extiende o finaliza tu sesión.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(ID_SESSION_EXPIRY, notification)
    }

    fun showAdminAlert(context: Context, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "admin_alerts")
        }
        val pending = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ADMIN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(ID_ADMIN_ALERT, notification)
    }
}
