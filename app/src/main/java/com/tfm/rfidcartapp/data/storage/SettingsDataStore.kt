package com.tfm.rfidcartapp.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.settingsDataStore by preferencesDataStore(name = "user_settings")

// Objeto que define las claves usadas en DataStore para guardar preferencias.
// En este caso solo tenemos una clave: ALLERGENS, que almacena un conjunto de strings.
private object PrefKeys {
    val ALLERGENS = stringSetPreferencesKey("allergens")
}

// Clase de datos que representa la configuración de la aplicación.
// Por ahora solo contiene el conjunto de alérgenos seleccionados por el usuario.
data class SettingsData(
    val allergens: Set<String> = emptySet() // Si no hay nada guardado, el valor por defecto es vacío
)

// Repositorio encargado de leer y guardar la configuración del usuario en DataStore.
class SettingsRepository(private val context: Context) {

    // Flujo reactivo (Flow) que expone los datos actuales de configuración.
    // Cada vez que cambien las preferencias en DataStore, se emitirá un nuevo objeto SettingsData.
    val data: Flow<SettingsData> =
        context.settingsDataStore.data.map { prefs ->
            SettingsData(
                allergens = prefs[PrefKeys.ALLERGENS] ?: emptySet() // Recupera la lista de alérgenos o vacío si no existe
            )
        }

    // Función suspendida que guarda un nuevo conjunto de alérgenos en DataStore.
    // Sobrescribe el valor anterior asociado a la clave ALLERGENS.
    suspend fun save(allergens: Set<String>) {
        context.settingsDataStore.edit { prefs ->
            prefs[PrefKeys.ALLERGENS] = allergens
        }
    }
}