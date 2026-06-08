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

    // IDs of admin notifications already surfaced as a system notification, so each
    // derived item raises a tray notification at most once across polls. The getter
    // returns a defensive copy: SharedPreferences may hand back a shared instance.
    var seenAlertIds: Set<String>
        get() = prefs.getStringSet("seen_alert_ids", emptySet())?.toSet() ?: emptySet()
        set(value) { prefs.edit().putStringSet("seen_alert_ids", value).apply() }

    // IDs of fines already surfaced to the driver as a system notification, so a newly
    // issued fine raises a tray notification at most once across polls.
    var seenFineIds: Set<String>
        get() = prefs.getStringSet("seen_fine_ids", emptySet())?.toSet() ?: emptySet()
        set(value) { prefs.edit().putStringSet("seen_fine_ids", value).apply() }
}
