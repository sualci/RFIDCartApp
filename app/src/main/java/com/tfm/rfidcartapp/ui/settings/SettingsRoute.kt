package com.yourpkg.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.tfm.rfidcartapp.ui.settings.SettingsViewModel
import com.tfm.rfidcartapp.ui.settings.SettingsScreen

@Composable
fun SettingsRoute(
    vm: SettingsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    SettingsScreen(
        userName = state.userName,
        selected = state.selectedAllergens,
        loading = state.loading,
        saving = state.saving,
        onNameChange = vm::onNameChange,
        onToggle = vm::onToggleAllergen,
        onSave = vm::save,
        onSelectAll = vm::selectAll,
        onClearAll = vm::clearAll
    )
}