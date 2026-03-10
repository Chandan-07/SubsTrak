package com.example.subscription.data.db

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object OnboardingPreference {

    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    val Context.dataStore by preferencesDataStore(name = "settings")

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
}