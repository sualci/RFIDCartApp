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
    val items by cartViewModel.items.collectAsState()
    var userAllergens by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        repository.data.map { it.allergens }.collectLatest { userAllergens = it }
    }

    // Create MQTT service bound to the ViewModel's scope
    val mqttService = remember { MqttService(cartViewModel.viewModelScope) }

    LaunchedEffect(Unit) {
        mqttService.connectAndSubscribe(topic = "r2000/tags")
        // Collect payloads and sync cart
        mqttService.incoming.collectLatest { payload ->
            runCatching {
                val msg = Json.decodeFromString<MqttTagMessage>(payload)
                cartViewModel.syncWithTags(msg.tags)
            }.onFailure { it.printStackTrace() }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mqttService.disconnect() }
    }

    CartScreen(items = items, userAllergens = userAllergens)
}

@Composable
private fun rememberRepository(): SettingsRepository {
    val ctx: Context = LocalContext.current
    return remember(ctx) { SettingsRepository(ctx) }
}
