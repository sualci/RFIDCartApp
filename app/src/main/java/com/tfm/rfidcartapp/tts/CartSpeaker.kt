package com.tfm.rfidcartapp.tts

import com.tfm.rfidcartapp.util.AllergenLabels
import android.speech.tts.TextToSpeech
import android.content.Context
import com.tfm.rfidcartapp.data.model.Product
import com.tfm.rfidcartapp.util.toPrice
import java.util.Locale

class CartSpeaker(
    private val context: Context,
    private val tts: TextToSpeech,
    private val userAllergens: () -> Set<String>
) {

    fun ensureLanguage(locale: Locale = Locale.getDefault()) {
        if (tts.voice?.locale != locale) tts.language = locale
    }

    fun speakDelta(delta: CartDelta, cartTotal: Double) {
        if (delta.isEmpty()) return  // <-- si no hay cambios

        val parts = mutableListOf<String>()

        delta.added.forEach { item ->
            val unitPrice = item.product.price.toPrice()
            parts += "Se añadió ${item.quantity} × ${item.product.name} (Prezo $unitPrice la unidad)."
            allergenWarning(item.product)?.let { parts += it }
        }
        delta.increased.forEach { c ->
            val diff = c.newQty - c.oldQty
            parts += "Se añadió $diff más de: ${c.product.name}."
            allergenWarning(c.product)?.let { parts += it }
        }
        delta.removed.forEach { item ->
            parts += "Se ha sacado ${item.quantity} × ${item.product.name}."
        }
        delta.decreased.forEach { c ->
            val diff = c.oldQty - c.newQty
            parts += "Sacado $diff de: ${c.product.name}."
        }

        parts += "Total compra: ${cartTotal.toPrice()}."  // <-- total siempre

        tts.speak(parts.joinToString(" "), TextToSpeech.QUEUE_FLUSH, null, "cart_tts_delta")
    }

    private fun allergenWarning(product: Product): String? {
        val matched = product.allergens.intersect(userAllergens())
        if (matched.isEmpty()) return null
        val labels = AllergenLabels.labelsFor(context, matched)
        return if (labels.isNotEmpty()) "Cuidado, alérgenos: ${labels.joinToString(", ")}." else null
    }
}
