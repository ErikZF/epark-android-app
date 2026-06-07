package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.data.repository.AuthState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Local, per-user cache of the user's registered vehicles and payment methods so the profile
 * summary stays viewable offline, extending the offline-first approach of requirement 13.
 *
 * Data is scoped per-user (logging in as another account never exposes prior data) and cleared
 * on logout via [clear].
 */
object ProfileCache {
    private const val PREFS = "epark_profile_cache"
    private const val KEY_VEHICLES_PREFIX = "cached_vehicles_"
    private const val KEY_METHODS_PREFIX = "cached_methods_"
    private const val KEY_SAVED_AT_PREFIX = "cached_profile_saved_at_"

    private val gson = Gson()
    private lateinit var ctx: Context

    fun init(context: Context) {
        ctx = context.applicationContext
    }

    private fun vehiclesKeyFor(userId: Int) = "$KEY_VEHICLES_PREFIX$userId"
    private fun methodsKeyFor(userId: Int) = "$KEY_METHODS_PREFIX$userId"
    private fun savedAtKeyFor(userId: Int) = "$KEY_SAVED_AT_PREFIX$userId"

    fun saveVehicles(vehicles: List<Vehicle>, userId: Int = AuthState.userId) {
        if (userId == 0) return
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(vehiclesKeyFor(userId), gson.toJson(vehicles))
            .putLong(savedAtKeyFor(userId), System.currentTimeMillis())
            .apply()
    }

    fun loadVehicles(userId: Int = AuthState.userId): List<Vehicle> {
        if (userId == 0) return emptyList()
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(vehiclesKeyFor(userId), null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Vehicle>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePaymentMethods(methods: List<PaymentMethod>, userId: Int = AuthState.userId) {
        if (userId == 0) return
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(methodsKeyFor(userId), gson.toJson(methods))
            .putLong(savedAtKeyFor(userId), System.currentTimeMillis())
            .apply()
    }

    fun loadPaymentMethods(userId: Int = AuthState.userId): List<PaymentMethod> {
        if (userId == 0) return emptyList()
        val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(methodsKeyFor(userId), null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<PaymentMethod>>() {}.type)
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

    /** Removes all cached profile data. Call on logout so the next user starts clean. */
    fun clear() {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
