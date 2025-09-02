package com.yourpkg.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.tfm.rfidcartapp.ui.settings.SettingsViewModel
import com.tfm.rfidcartapp.ui.settings.SettingsScreen

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = viewModel() // ViewModel para la pantalla de ajustes
) {
    // Estado de la UI expuesto por el ViewModel (se actualiza en tiempo real)
    val state by vm.uiState.collectAsState()

    // Dibuja la pantalla de ajustes pasando callbacks y estado actual
    SettingsScreen(
        selected = state.selectedAllergens,   // alérgenos seleccionados por el usuario
        loading = state.loading,              // indica si está cargando datos
        saving = state.saving,                // indica si está guardando cambios
        onToggle = vm::onToggleAllergen,      // selecciona alergeno
        onSave = vm::save,                    // guarda la configuración
        onSelectAll = vm::selectAll,          // selecciona todos los alérgenos
        onClearAll = vm::clearAll             // desmarca todos los alérgenos
    )
}
