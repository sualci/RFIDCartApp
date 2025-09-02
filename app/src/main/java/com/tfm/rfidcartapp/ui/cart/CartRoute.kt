package com.tfm.rfidcartapp.ui.cart

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tfm.rfidcartapp.data.storage.SettingsRepository
import com.tfm.rfidcartapp.mqtt.MqttService
import com.tfm.rfidcartapp.mqtt.MqttTagMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Composable
fun CartRoute(
    repository: SettingsRepository = rememberRepository(),
    cartViewModel: CartViewModel = viewModel()
) {
    // Estado de los ítems del carrito (se actualiza automáticamente)
    val items by cartViewModel.items.collectAsState()

    // Estado local con los alérgenos configurados por el usuario
    var userAllergens by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Efecto lanzado al inicio: escuchar cambios en las preferencias (DataStore)
    LaunchedEffect(Unit) {
        repository.data.map { it.allergens }.collectLatest { userAllergens = it }
    }

    // Crear servicio MQTT ligado al scope del ViewModel
    val mqttService = remember { MqttService(cartViewModel.viewModelScope) }

    // Efecto lanzado al inicio: conectar y suscribirse a MQTT
    LaunchedEffect(Unit) {
        mqttService.connectAndSubscribe(topic = "r2000/tags")
        // Recibir mensajes MQTT y sincronizar el carrito
        mqttService.incoming.collectLatest { payload ->
            runCatching {
                val msg = Json.decodeFromString<MqttTagMessage>(payload)
                cartViewModel.syncWithTags(msg.tags)
            }.onFailure { it.printStackTrace() }
        }
    }

    // Asegura desconexión limpia de MQTT cuando se destruya el Composable
    DisposableEffect(Unit) {
        onDispose { mqttService.disconnect() }
    }

    // Dibuja la pantalla del carrito con los ítems y alérgenos del usuario
    CartScreen(items = items, userAllergens = userAllergens)
}

@Composable
// Crea y recuerda el repositorio de configuración (SettingsRepository)
private fun rememberRepository(): SettingsRepository {
    val ctx: Context = LocalContext.current
    return remember(ctx) { SettingsRepository(ctx) }
}
