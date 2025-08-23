package com.tfm.rfidcartapp.ui.cart

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.model.Product

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    fun add(product: Product) {
        _items.update { current ->
            val idx = current.indexOfFirst { it.product.id == product.id }
            if (idx >= 0) {
                current.toMutableList().apply {
                    val existing = this[idx]
                    this[idx] = existing.copy(quantity = existing.quantity + 1)
                }
            } else {
                current + CartItem(product, 1)
            }
        }
    }

    fun totalPrice(): Double = _items.value.sumOf { it.product.price * it.quantity }
}
