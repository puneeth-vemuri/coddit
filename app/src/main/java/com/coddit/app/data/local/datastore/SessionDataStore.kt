package com.coddit.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val USER_ID = stringPreferencesKey("user_id")
    private val USERNAME = stringPreferencesKey("username")
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")

    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME] }
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val recentSearches: Flow<Set<String>> = context.dataStore.data.map { it[RECENT_SEARCHES] ?: emptySet() }

    suspend fun saveSession(uid: String, name: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = uid
            if (name != null) preferences[USERNAME] = name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[RECENT_SEARCHES] ?: emptySet()
            preferences[RECENT_SEARCHES] = current.toMutableSet().apply {
                if (size >= 10) remove(first())
                add(query)
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
