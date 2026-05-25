package com.samiuysal.fediversehub.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samiuysal.fediversehub.core.model.PlatformType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val themeMode: Flow<ThemeMode> = context.appPreferencesDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]?.let(ThemeMode::fromStorageValue) ?: ThemeMode.SYSTEM
    }

    val onboardingSeen: Flow<Boolean> = context.appPreferencesDataStore.data.map { preferences ->
        preferences[ONBOARDING_SEEN_KEY] ?: false
    }

    val selectedPlatform: Flow<PlatformType> = context.appPreferencesDataStore.data.map { preferences ->
        preferences[SELECTED_PLATFORM_KEY]?.let { value ->
            runCatching { PlatformType.valueOf(value) }.getOrNull()
        } ?: PlatformType.MASTODON
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.storageValue
        }
    }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[ONBOARDING_SEEN_KEY] = seen
        }
    }

    suspend fun setSelectedPlatform(platform: PlatformType) {
        context.appPreferencesDataStore.edit { preferences ->
            preferences[SELECTED_PLATFORM_KEY] = platform.name
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val ONBOARDING_SEEN_KEY = booleanPreferencesKey("onboarding_seen")
        val SELECTED_PLATFORM_KEY = stringPreferencesKey("selected_platform")
    }
}

enum class ThemeMode(val storageValue: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromStorageValue(value: String): ThemeMode? =
            entries.firstOrNull { it.storageValue == value }
    }
}
