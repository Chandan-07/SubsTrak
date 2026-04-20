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
    private val LOGGED_IN_KEY = booleanPreferencesKey("logged_in") // 👈 NEW

    val Context.dataStore by preferencesDataStore(name = "settings")

    // ✅ Onboarding
    suspend fun setCompleted(context: Context) {
        context.dataStore.edit {
            it[ONBOARDING_KEY] = true
        }
    }

    fun isCompleted(context: Context): Flow<Boolean> {
        return context.dataStore.data.map {
            it[ONBOARDING_KEY] ?: false
        }
    }

    // ✅ Auth Skipped
    suspend fun setAuthSkipped(context: Context) {
        context.dataStore.edit {
            it[AUTH_SKIPPED_KEY] = true
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
}