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
    private const val CHANNEL_FINE    = "epark_fine"
    private const val ID_SESSION_EXPIRY = 1001
    private const val ID_ADMIN_ALERT    = 1002
    private const val ID_WELCOME        = 1003
    private const val ID_FINE           = 1004

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
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_FINE, "Multas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos cuando recibes una multa"
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

    /** Welcome notification shown once the account has been created and verified. */
    fun showWelcome(context: Context, fullName: String) {
        val firstName = fullName.trim().split(" ").firstOrNull().orEmpty()
        val title = if (firstName.isBlank()) "¡Bienvenido a epark!" else "¡Bienvenido a epark, $firstName!"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "notifications")
        }
        val pending = PendingIntent.getActivity(
            context, ID_WELCOME, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_SESSION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Tu cuenta fue creada y verificada con éxito. ¡Ya puedes empezar a parquear!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(ID_WELCOME, notification)
    }

    /**
     * Notifies a driver that an admin issued them a fine. Tapping it opens the History
     * screen (Multas tab) where the driver can review and pay. A distinct notification id
     * per fine keeps concurrent fines from overwriting each other in the tray.
     */
    fun showFineIssued(context: Context, fineId: String, zoneName: String, reason: String, amount: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "history")
        }
        val notifId = ID_FINE + (fineId.hashCode() and 0xFFFF)
        val pending = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_FINE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚫 Nueva multa recibida")
            .setContentText("$reason en $zoneName · $amount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun showAdminAlert(context: Context, title: String, body: String, alertId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to", "admin_alert_detail")
            putExtra("alert_id", alertId)
        }
        // Distinct requestCode + notification id per alert so concurrent alerts don't
        // overwrite each other's pending intent or tray entry.
        val notifId = ID_ADMIN_ALERT + (alertId.hashCode() and 0xFFFF)
        val pending = PendingIntent.getActivity(
            context, notifId, intent,
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

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
