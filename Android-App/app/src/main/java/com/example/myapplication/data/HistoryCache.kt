package com.example.myapplication.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryCache {
    private const val PREFS = "epark_history_cache"
    private const val KEY_SESSIONS = "cached_sessions"
    private val gson = Gson()
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    fun saveSessions(sessions: List<ParkingSession>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SESSIONS, gson.toJson(sessions))
            .apply()
    }

    fun loadSessions(): List<ParkingSession> {
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ParkingSession>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
