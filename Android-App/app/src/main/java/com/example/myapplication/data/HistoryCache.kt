package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.repository.AuthState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Local, offline-first cache of the user's recent parking session history (requirement 13.1).
 *
 * Sessions are stored per-user so logging in as a different account never exposes a previous
 * user's history, and only the most recent [MAX_SESSIONS] are kept. The cache is cleared on logout.
 */
object HistoryCache {
    private const val PREFS = "epark_history_cache"
    private const val KEY_SESSIONS_PREFIX = "cached_sessions_"
    private const val MAX_SESSIONS = 20

    private val gson = Gson()
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private fun keyFor(userId: Int) = "$KEY_SESSIONS_PREFIX$userId"

    fun saveSessions(sessions: List<ParkingSession>, userId: Int = AuthState.userId) {
        if (userId == 0) return
        val recent = sessions.take(MAX_SESSIONS)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), gson.toJson(recent))
            .apply()
    }

    fun loadSessions(userId: Int = AuthState.userId): List<ParkingSession> {
        if (userId == 0) return emptyList()
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(keyFor(userId), null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ParkingSession>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Removes all cached history. Call on logout so the next user starts clean. */
    fun clear() {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
