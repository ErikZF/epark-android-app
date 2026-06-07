package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.repository.AuthState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local, per-user store of the in-app notifications shown in the Notifications screen.
 *
 * Notifications are persisted with SharedPreferences (newest first) and scoped per-user so a
 * different account never sees another user's notifications. Only the most recent
 * [MAX_NOTIFICATIONS] are kept, and the store is cleared on logout.
 */
object NotificationStore {
    private const val PREFS = "epark_notifications"
    private const val KEY_PREFIX = "notifications_"
    private const val MAX_NOTIFICATIONS = 30

    private val gson = Gson()
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private fun keyFor(userId: Int) = "$KEY_PREFIX$userId"

    /** Adds a notification at the top of the user's list. */
    fun add(title: String, body: String, userId: Int = AuthState.userId) {
        if (userId == 0) return
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val item = NotificationItem(
            id = "n_${System.currentTimeMillis()}",
            title = title,
            body = body,
            time = time,
        )
        val updated = (listOf(item) + all(userId)).take(MAX_NOTIFICATIONS)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), gson.toJson(updated))
            .apply()
    }

    /** Convenience helper: the welcome notification stored right after registration. */
    fun addWelcome(fullName: String, userId: Int = AuthState.userId) {
        val firstName = fullName.trim().split(" ").firstOrNull().orEmpty()
        val greeting = if (firstName.isBlank()) "¡Bienvenido a epark!" else "¡Bienvenido a epark, $firstName!"
        add(
            title = greeting,
            body = "Tu cuenta fue creada y verificada con éxito. Ya puedes registrar tus vehículos y parquear.",
            userId = userId,
        )
    }

    fun all(userId: Int = AuthState.userId): List<NotificationItem> {
        if (userId == 0) return emptyList()
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(keyFor(userId), null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<NotificationItem>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Removes all stored notifications. Call on logout so the next user starts clean. */
    fun clear() {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
