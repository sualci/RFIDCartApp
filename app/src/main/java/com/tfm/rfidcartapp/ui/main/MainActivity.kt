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

    private val cartViewModel: CartViewModel by viewModels() // <- una sola instancia
    private lateinit var tts: TextToSpeech

    private lateinit var speaker: CartSpeaker

    @Volatile
    private var userAllergens: Set<String> = emptySet()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        ensureNotificationPermission()

        // Init TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.getDefault()
            }
        }

        // inicializar el speaker pasando el TTS y un acceso a los alérgenos del usuario
        speaker = CartSpeaker(
            context = this, tts = tts, userAllergens = { userAllergens })


        // observes DataStore to keep userAllergens updated
        val settingsRepository = SettingsRepository(applicationContext)
        lifecycleScope.launch {
            settingsRepository.data.map { it.allergens }.collect { allergens ->
                    userAllergens = allergens
                }
        }

        // observador de cambios del carrito
        observeCartVoiceNotifications()

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

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    @OptIn(FlowPreview::class)
    private fun observeCartVoiceNotifications() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                var last: List<CartItem> = emptyList()
                cartViewModel.items.debounce(200).distinctUntilChanged().collect { current ->
                        val delta = CartDeltaCalculator.compute(last, current)
                        if (!delta.isEmpty()) {
                            val total = cartViewModel.totalPrice()
                            speaker.ensureLanguage(Locale.getDefault())
                            speaker.speakDelta(
                                delta, total
                            )  // dice el total después de los cambios
                            last = current
                        } else {
                            // Sin cambios: no hablar y no actualizamos 'last' para seguir comparando correctamente
                            last = current
                        }
                    }
            }
        }
    }

    private val requestNotifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // log
    }

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