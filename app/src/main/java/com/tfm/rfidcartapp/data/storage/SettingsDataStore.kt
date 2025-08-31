package com.tfm.rfidcartapp.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.settingsDataStore by preferencesDataStore(name = "user_settings")

private object PrefKeys {
    val ALLERGENS = stringSetPreferencesKey("allergens")
}

data class SettingsData(
    val allergens: Set<String> = emptySet()
)

class SettingsRepository(private val context: Context) {
    val data: Flow<SettingsData> =
        context.settingsDataStore.data.map { prefs ->
            SettingsData(
                allergens = prefs[PrefKeys.ALLERGENS] ?: emptySet()
            )
        }

    suspend fun save(allergens: Set<String>) {
        context.settingsDataStore.edit { prefs ->
            prefs[PrefKeys.ALLERGENS] = allergens
        }
    }
}
