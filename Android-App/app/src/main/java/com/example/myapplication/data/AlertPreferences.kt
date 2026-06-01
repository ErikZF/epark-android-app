package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

object AlertPreferences {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("epark_prefs", Context.MODE_PRIVATE)
    }

    var alertMinutes: Int
        get() = prefs.getInt("alert_minutes", 10)
        set(value) { prefs.edit().putInt("alert_minutes", value).apply() }
}
