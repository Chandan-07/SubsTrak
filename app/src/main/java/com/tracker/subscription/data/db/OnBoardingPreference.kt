package com.tracker.subscription.data.db

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object OnboardingPreference {

    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    private val AUTH_SKIPPED_KEY = booleanPreferencesKey("auth_skipped")
    private val LOGGED_IN_KEY = booleanPreferencesKey("logged_in")
    private val GUEST_PREMIUM_KEY = booleanPreferencesKey("guest_premium_owned")
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")

    val Context.dataStore by preferencesDataStore(name = "settings")

    // ✅ Onboarding
    suspend fun setCompleted(context: Context, value: Boolean = true) {
        context.dataStore.edit {
            it[ONBOARDING_KEY] = value
        }
    }

    fun isCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[ONBOARDING_KEY] ?: false
        }
    }

    // ✅ Auth Skipped
    suspend fun setAuthSkipped(context: Context, value: Boolean = true) {
        context.dataStore.edit {
            it[AUTH_SKIPPED_KEY] = value
        }
    }

    fun isAuthSkipped(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[AUTH_SKIPPED_KEY] ?: false
        }
    }

    // 🔥 NEW: Logged In
    suspend fun setLoggedIn(context: Context, value: Boolean) {
        context.dataStore.edit {
            it[LOGGED_IN_KEY] = value
        }
    }

    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[LOGGED_IN_KEY] ?: false
        }
    }

    suspend fun setGuestPremiumOwned(context: Context, value: Boolean) {
        context.dataStore.edit {
            it[GUEST_PREMIUM_KEY] = value
        }
    }

    fun isGuestPremiumOwned(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[GUEST_PREMIUM_KEY] ?: false
        }
    }

    // 🎨 Theme Preference
    suspend fun setDarkTheme(context: Context, isDark: Boolean) {
        context.dataStore.edit {
            it[DARK_THEME_KEY] = isDark
        }
    }

    fun isDarkTheme(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[DARK_THEME_KEY] ?: false
        }
    }
}