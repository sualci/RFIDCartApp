package com.tfm.rfidcartapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.data.model.Allergens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selected: Set<String>, // Conjunto de alérgenos seleccionados
    loading: Boolean,      // Estado de carga inicial
    saving: Boolean,       // Estado de guardado
    onToggle: (String) -> Unit, // Acción al marcar/desmarcar un alérgeno
    onSave: () -> Unit,         // Acción al pulsar "Guardar"
    onSelectAll: () -> Unit,    // Acción para seleccionar todos los alérgenos
    onClearAll: () -> Unit      // Acción para limpiar selección
) {
    val focus = LocalFocusManager.current
    var isEditing by rememberSaveable { mutableStateOf(false) } // Controla si se está editando

    // Estado para mostrar snackbars
    val snackbarHostState = remember { SnackbarHostState() }
    var wasSaving by remember { mutableStateOf(false) }

    // Efecto que detecta cuando se termina de guardar
    LaunchedEffect(saving) {
        if (wasSaving && !saving) {
            isEditing = false
            focus.clearFocus()
            snackbarHostState.showSnackbar(
                message = "Guardado", // Mensaje de confirmación
                duration = SnackbarDuration.Short
            )
        }
        wasSaving = saving
    }

    // Control del switch "Seleccionar todo"
    var selectAll by remember(selected) {
        mutableStateOf(selected.size == Allergens.all.size)
    }
    LaunchedEffect(selected) {
        selectAll = selected.size == Allergens.all.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                actions = {
                    // Botón para activar/desactivar modo edición
                    IconButton(onClick = {
                        if (isEditing) {
                            focus.clearFocus()
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                            contentDescription = (if (isEditing) stringResource(id = R.string.btn_cancel) else stringResource(id = R.string.btn_edit))
                        )
                    }
                }
            )
        },
        // Host para mostrar snackbars (alertas en la parte inferior)
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2E7D32), // Verde personalizado
                    contentColor = Color.White,
                    actionColor = Color.White
                )
            }
        }
    ) { innerPadding ->

        // Si está cargando, mostrar un loader centrado
        if (loading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fila superior con título + switch seleccionar todo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = R.string.allergens_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = selectAll,
                    onCheckedChange = { checked ->
                        if (isEditing) {
                            selectAll = checked
                            if (checked) onSelectAll() else onClearAll()
                        }
                    },
                    enabled = isEditing
                )
            }

            // Lista de alérgenos con checkboxes
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Allergens.all) { allergen ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(id = allergen.labelRes),
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = allergen.id in selected,
                            onCheckedChange = {
                                if (isEditing) onToggle(allergen.id)
                            },
                            enabled = isEditing
                        )
                    }
                }
            }

            // Botón guardar (solo en modo edición)
            if (isEditing) {
                Button(
                    onClick = {
                        focus.clearFocus()
                        onSave()
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (saving) stringResource(id = R.string.msg_saving)
                        else stringResource(id = R.string.btn_save)
                    )
                }
            }
        }
    }
}
