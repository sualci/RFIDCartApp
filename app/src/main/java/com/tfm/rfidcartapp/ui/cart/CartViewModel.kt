package com.tfm.rfidcartapp.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.repository.ProductRepository
import com.tfm.rfidcartapp.data.repository.tagToProductId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {
    // Estado interno del carrito (lista de ítems)
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    // Exposición pública del carrito como flujo inmutable
    val items: StateFlow<List<CartItem>> = _items

    // Calcula el precio total del carrito
    fun totalPrice(): Double = _items.value.sumOf { it.product.price * it.quantity }

    // Sincroniza el carrito completo a partir de una lista de tags EPC detectados
    fun syncWithTags(tags: List<String>) {
        runCatching {
            Log.d("CartVM", "Incoming tags: $tags")
            val counts = mutableMapOf<String, Int>() // mapa productoId → cantidad

            // Cuenta cuántas veces aparece cada producto según las etiquetas detectadas
            for (tag in tags) {
                val productId = tagToProductId[tag] ?: continue
                Log.d("CartVM", "No tag $tag -> $productId")
                counts[productId] = (counts[productId] ?: 0) + 1
            }

            // Crea la nueva lista de ítems del carrito con productos y cantidades
            val newItems = counts.mapNotNull { (productId, qty) ->
                ProductRepository.getById(productId)?.let { product ->
                    CartItem(product, qty)
                }
            }

            Log.d("CartVM", "New cart: $newItems")
            _items.value = newItems // actualiza el estado
        }.onFailure { Log.e("CartVM", "syncWithTags failed", it)}
    }
}
