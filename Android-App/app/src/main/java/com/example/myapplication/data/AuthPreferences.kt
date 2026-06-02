package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.remote.AuthResponseDto
import kotlinx.coroutines.flow.first

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

object AuthPreferences {
    private lateinit var appContext: Context

    private val KEY_USER_ID = intPreferencesKey("user_id")
    private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_ROLE = stringPreferencesKey("role")
    private val KEY_MUNICIPALITY_ID = intPreferencesKey("municipality_id")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun save(auth: AuthResponseDto) {
        appContext.authDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = auth.userId
            prefs[KEY_FULL_NAME] = auth.fullName
            prefs[KEY_EMAIL] = auth.email
            prefs[KEY_ROLE] = auth.role
            prefs[KEY_MUNICIPALITY_ID] = auth.municipalityId ?: 0
        }
    }

    /** Returns stored auth if a valid session exists, null otherwise. */
    suspend fun load(): AuthResponseDto? {
        val prefs = appContext.authDataStore.data.first()
        val userId = prefs[KEY_USER_ID]?.takeIf { it != 0 } ?: return null
        return AuthResponseDto(
            userId = userId,
            fullName = prefs[KEY_FULL_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            role = prefs[KEY_ROLE] ?: "",
            municipalityId = prefs[KEY_MUNICIPALITY_ID],
        )
    }

    suspend fun clear() {
        appContext.authDataStore.edit { it.clear() }
    }
}
