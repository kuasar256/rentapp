package com.example.rentapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rentapp_preferences")

object PreferencesManager {
    private val LANGUAGE_KEY         = stringPreferencesKey("app_language")
    private val CURRENCY_KEY         = stringPreferencesKey("app_currency")
    private val BIOMETRIC_ENABLED    = booleanPreferencesKey("biometric_enabled")
    private val LAST_AUTH_TIMESTAMP  = longPreferencesKey("last_auth_timestamp")
    private val DARK_MODE_ENABLED    = booleanPreferencesKey("dark_mode_enabled")

    // ── Tema Oscuro ──────────────────────────────────────────────────────────
    fun getDarkModeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_MODE_ENABLED] ?: true }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE_ENABLED] = enabled }
    }

    // ── Idioma ───────────────────────────────────────────────────────────────
    fun getLanguageFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[LANGUAGE_KEY] ?: "es" }

    suspend fun setLanguage(context: Context, language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }

    // ── Moneda ───────────────────────────────────────────────────────────────
    fun getCurrencyFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[CURRENCY_KEY] ?: "USD" }

    suspend fun setCurrency(context: Context, currency: String) {
        context.dataStore.edit { it[CURRENCY_KEY] = currency }
    }

    // ── Biometría ────────────────────────────────────────────────────────────
    fun getBiometricEnabledFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }

    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    fun getLastAuthTimestampFlow(context: Context): Flow<Long> =
        context.dataStore.data.map { it[LAST_AUTH_TIMESTAMP] ?: 0L }

    suspend fun setLastAuthTimestamp(context: Context, timestamp: Long) {
        context.dataStore.edit { it[LAST_AUTH_TIMESTAMP] = timestamp }
    }
}
