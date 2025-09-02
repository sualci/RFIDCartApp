package com.tfm.rfidcartapp.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tfm.rfidcartapp.data.model.Allergens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.tfm.rfidcartapp.data.storage.SettingsRepository
// Estado de la pantalla de ajustes (UI State)
data class SettingsUiState(
    val userName: String = "",                        // Nombre de usuario (opcional, por ahora vacío)
    val selectedAllergens: Set<String> = emptySet(),  // Conjunto de alérgenos seleccionados
    val loading: Boolean = true,                      // Indica si se está cargando la configuración
    val saving: Boolean = false,                      // Indica si se está guardando
    val message: String? = null                       // Mensaje para mostrar al usuario (ej: "Guardado")
)

// ViewModel que gestiona la lógica de la pantalla de ajustes
class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    // Repositorio que maneja la persistencia de los datos
    private val repo = SettingsRepository(app.applicationContext)

    // Estado interno mutable de la UI
    private val _uiState = MutableStateFlow(SettingsUiState())
    // Estado expuesto de solo lectura (la vista solo observa)
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        // Al inicializar, se observa el repositorio y se actualiza el estado
        viewModelScope.launch {
            repo.data.collect { stored ->
                _uiState.update {
                    it.copy(
                        selectedAllergens = stored.allergens, // Cargar alérgenos guardados
                        loading = false,                      // Ya no está cargando
                        message = null                        // Se limpia mensaje previo
                    )
                }
            }
        }
    }

    // Alterna un alérgeno en la lista (lo agrega o lo quita)
    fun onToggleAllergen(id: String) {
        _uiState.update {
            val set = it.selectedAllergens.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            it.copy(selectedAllergens = set, message = null)
        }
    }

    // Selecciona todos los alérgenos disponibles
    fun selectAll() {
        _uiState.update { it.copy(selectedAllergens = Allergens.all.map { a -> a.id }.toSet()) }
    }

    // Limpia la selección de alérgenos
    fun clearAll() {
        _uiState.update { it.copy(selectedAllergens = emptySet()) }
    }

    // Guarda los cambios en el repositorio
    fun save() {
        val snapshot = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, message = null) } // Estado: guardando
            repo.save(snapshot.selectedAllergens)                      // Guardar en repositorio
            _uiState.update { it.copy(saving = false, message = "Guardado") } // Estado: guardado
        }
    }
}