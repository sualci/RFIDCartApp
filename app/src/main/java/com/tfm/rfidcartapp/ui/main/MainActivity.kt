package com.tfm.rfidcartapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.storage.SettingsRepository
import com.tfm.rfidcartapp.tts.CartDeltaCalculator
import com.tfm.rfidcartapp.tts.CartSpeaker
import com.tfm.rfidcartapp.ui.cart.CartViewModel
import com.tfm.rfidcartapp.ui.login.AppNav
import com.tfm.rfidcartapp.ui.theme.RFIDCartAppTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : ComponentActivity() {

    // ViewModel del carrito (una sola instancia en toda la actividad)
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var tts: TextToSpeech // motor de texto a voz

    private lateinit var speaker: CartSpeaker // clase que gestiona los avisos hablados

    @Volatile
    private var userAllergens: Set<String> = emptySet() // alérgenos configurados por el usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen() // muestra pantalla de splash
        ensureNotificationPermission() // pide permiso de notificaciones si es necesario

        // Inicializa el motor de texto a voz (TTS)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault() // configura idioma por defecto
            }
        }

        // Inicializa el speaker con TTS y acceso a los alérgenos del usuario
        speaker = CartSpeaker(
            context = this, tts = tts, userAllergens = { userAllergens }
        )

        // Observa el DataStore para mantener los alérgenos actualizados
        val settingsRepository = SettingsRepository(applicationContext)
        lifecycleScope.launch {
            settingsRepository.data.map { it.allergens }.collect { allergens ->
                userAllergens = allergens
            }
        }

        // Observa los cambios del carrito y lanza notificaciones de voz
        observeCartVoiceNotifications()

        // Dibuja la interfaz principal
        setContent {
            RFIDCartAppTheme {
                AppNav(cartViewModel = cartViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    // Cierra el motor de TTS al destruir la actividad
    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    @OptIn(FlowPreview::class)
    // Observa cambios en el carrito y hace que el altavoz hable con el delta detectado
    private fun observeCartVoiceNotifications() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var last: List<CartItem> = emptyList() // estado anterior del carrito
                cartViewModel.items
                    .debounce(200) // espera brevemente para evitar spam de cambios
                    .distinctUntilChanged() // ignora si la lista no cambió
                    .collect { current ->
                        val delta = CartDeltaCalculator.compute(last, current)
                        if (!delta.isEmpty()) {
                            val total = cartViewModel.totalPrice()
                            speaker.ensureLanguage(Locale.getDefault())
                            speaker.speakDelta(delta, total) // lee en voz alta los cambios
                            last = current
                        } else {
                            // Sin cambios: actualiza igualmente el estado
                            last = current
                        }
                    }
            }
        }
    }

    // Resultado de la petición de permiso de notificaciones
    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // aquí se podría registrar en logs si fue concedido o no
    }

    // Comprueba y pide permiso de notificaciones (Android 13+)
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}