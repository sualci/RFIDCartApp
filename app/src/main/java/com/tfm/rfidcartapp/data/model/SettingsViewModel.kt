package com.tfm.rfidcartapp.data.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.tfm.rfidcartapp.data.storage.SettingsRepository
data class SettingsUiState(
    val userName: String = "",
    val selectedAllergens: Set<String> = emptySet(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app.applicationContext)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            repo.data.collect { stored ->
                _uiState.update {
                    it.copy(
                        userName = stored.userName,
                        selectedAllergens = stored.allergens,
                        loading = false,
                        message = null
                    )
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(userName = newName, message = null) }
    }

    fun onToggleAllergen(id: String) {
        _uiState.update {
            val set = it.selectedAllergens.toMutableSet()
            if (set.contains(id)) set.remove(id) else set.add(id)
            it.copy(selectedAllergens = set, message = null)
        }
    }

    fun selectAll() {
        _uiState.update { it.copy(selectedAllergens = Allergens.all.map { a -> a.id }.toSet()) }
    }

    fun clearAll() {
        _uiState.update { it.copy(selectedAllergens = emptySet()) }
    }

    fun save() {
        val snapshot = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, message = null) }
            repo.save(snapshot.userName, snapshot.selectedAllergens)
            _uiState.update { it.copy(saving = false, message = "Guardado") }
        }
    }
}
