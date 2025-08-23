package com.tfm.rfidcartapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.model.Product
import com.tfm.rfidcartapp.data.repository.ProductRepository
import com.tfm.rfidcartapp.data.storage.SettingsRepository
import com.tfm.rfidcartapp.ui.cart.CartViewModel
import com.tfm.rfidcartapp.ui.login.AppNav
import com.tfm.rfidcartapp.ui.theme.RFIDCartAppTheme
import com.tfm.rfidcartapp.util.AllergenLabels
import com.tfm.rfidcartapp.util.toPrice
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val cartViewModel: CartViewModel by viewModels() // <- una sola instancia
    private lateinit var tts: TextToSpeech
    private var nfcAdapter: NfcAdapter? = null

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

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)


        // observes DataStore to keep userAllergens updated
        val settingsRepository = SettingsRepository(applicationContext)
        lifecycleScope.launch {
            settingsRepository.data
                .map { it.allergens }
                .collect { allergens ->
                    userAllergens = allergens
                }
        }

        setContent {
            RFIDCartAppTheme {
                AppNav(cartViewModel = cartViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this, this, NfcAdapter.FLAG_READER_NFC_A, null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    // ReaderCallback
    override fun onTagDiscovered(tag: Tag?) {
        Log.d("NFC_DEBUG", "Tag discovered")
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            Log.w("NFC_DEBUG", "Not an NDEF tag")
            return
        }
        try {
            ndef.connect()
            val msg = ndef.ndefMessage ?: ndef.cachedNdefMessage
            if (msg == null) {
                Log.w("NFC_DEBUG", "No NDEF message")
                return
            }

            val record = msg.records.first()
            val productId = parseText(record) // o parseUri
            Log.d("NFC_DEBUG", "Parsed productId=$productId")

            if (!productId.isNullOrBlank()) {
                // Call your handler on the UI thread so it can update ViewModel/notify/TTS
                runOnUiThread {
                    handleScannedProduct(productId)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "No productId in tag", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("NFC_DEBUG", "Error reading tag", e)
        } finally {
            try {
                ndef.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun parseText(record: NdefRecord): String? {
        val payload = record.payload ?: return null
        val status = payload[0].toInt()
        val langLength = status and 0x3F
        val textBytes = payload.copyOfRange(1 + langLength, payload.size)
        return textBytes.toString(Charsets.UTF_8)
    }

    private fun handleScannedProduct(productId: String) {
        val product = ProductRepository.getById(productId) ?: return
        cartViewModel.add(product)

        val items = cartViewModel.items.value
        val cartItem = items.firstOrNull { it.product.id == product.id } ?: return
        val total = cartViewModel.totalPrice()

        showProductNotification(product)
        speakCartItem(cartItem, total)
    }

    private fun showProductNotification(product: Product) {
        // for android >=13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // (optinal) shot requestPermission
                return
            }
        }

        val notif =
            NotificationCompat.Builder(this, "cart_channel").setSmallIcon(R.drawable.shop_cart)
                .setContentTitle("Product added")
                .setContentText("${product.name} • ${"%.2f€".format(product.price)}")
                .setAutoCancel(true).build()

        NotificationManagerCompat.from(this)
            .notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
    }

    private fun speakCartItem(cartItem: CartItem, cartTotal: Double) {
        val product = cartItem.product
        val quantity = cartItem.quantity

        // Intersect product allergens with userAllergens (kept in MainActivity)
        val matched = product.allergens.intersect(userAllergens)
        val labels = AllergenLabels.labelsFor(this, matched)
        val allergenWarning =
            if (labels.isNotEmpty()) "Cuidado, alergenos: ${labels.joinToString(", ")}. " else ""

        val spoken = buildString {
            append("${product.name}, precio ${product.price.toPrice()}. ")
            if (allergenWarning.isNotEmpty()) append(allergenWarning)
            append("Ahora tienes $quantity en el carro. ")
            append("Total compra: ${cartTotal.toPrice()}.")
        }

        tts.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "nfc_tts")
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