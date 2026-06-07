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
    private const val KEY_SAVED_AT_PREFIX = "cached_saved_at_"
    private const val KEY_FINES_PREFIX = "cached_fines_"
    private const val KEY_FINES_SAVED_AT_PREFIX = "cached_fines_saved_at_"
    private const val MAX_SESSIONS = 20
    private const val MAX_FINES = 20

    private val gson = Gson()
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private fun keyFor(userId: Int) = "$KEY_SESSIONS_PREFIX$userId"
    private fun savedAtKeyFor(userId: Int) = "$KEY_SAVED_AT_PREFIX$userId"
    private fun finesKeyFor(userId: Int) = "$KEY_FINES_PREFIX$userId"
    private fun finesSavedAtKeyFor(userId: Int) = "$KEY_FINES_SAVED_AT_PREFIX$userId"

    fun saveSessions(sessions: List<ParkingSession>, userId: Int = AuthState.userId) {
        if (userId == 0) return
        val recent = sessions.take(MAX_SESSIONS)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(keyFor(userId), gson.toJson(recent))
            .putLong(savedAtKeyFor(userId), System.currentTimeMillis())
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

    /**
     * Caches the user's fines (both pending and paid) so they remain viewable offline,
     * mirroring how sessions are cached for requirement 13.
     */
    fun saveFines(fines: List<Fine>, userId: Int = AuthState.userId) {
        if (userId == 0) return
        val recent = fines.take(MAX_FINES)
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(finesKeyFor(userId), gson.toJson(recent))
            .putLong(finesSavedAtKeyFor(userId), System.currentTimeMillis())
            .apply()
    }

    fun loadFines(userId: Int = AuthState.userId): List<Fine> {
        if (userId == 0) return emptyList()
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(finesKeyFor(userId), null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Fine>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Epoch millis of the last successful save, or 0 if nothing is cached. */
    fun lastSavedAt(userId: Int = AuthState.userId): Long {
        if (userId == 0) return 0L
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(savedAtKeyFor(userId), 0L)
    }

    /** Epoch millis of the last successful fines save, or 0 if nothing is cached. */
    fun finesLastSavedAt(userId: Int = AuthState.userId): Long {
        if (userId == 0) return 0L
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(finesSavedAtKeyFor(userId), 0L)
    }

    /** Removes all cached history. Call on logout so the next user starts clean. */
    fun clear() {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
