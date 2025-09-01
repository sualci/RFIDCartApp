package com.tfm.rfidcartapp.ui.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.repository.ProductRepository
import com.tfm.rfidcartapp.data.repository.tagToProductId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    fun totalPrice(): Double = _items.value.sumOf { it.product.price * it.quantity }

    // Replaces the whole cart based on the current set of tag EPCs.
    fun syncWithTags(tags: List<String>) {
        runCatching {
            Log.d("CartVM", "Incoming tags: $tags")
            val counts = mutableMapOf<String, Int>()
            for (tag in tags) {
                val productId = tagToProductId[tag] ?: continue
                Log.d("CartVM", "No tag $tag -> $productId")
                counts[productId] = (counts[productId] ?: 0) + 1
            }
            val newItems = counts.mapNotNull { (productId, qty) ->
                ProductRepository.getById(productId)?.let { product ->
                    CartItem(product, qty)
                }
            }
            Log.d("CartVM", "New cart: $newItems")
            _items.value = newItems
        }.onFailure { Log.e("CartVM", "syncWithTags failed", it)}
    }
}
