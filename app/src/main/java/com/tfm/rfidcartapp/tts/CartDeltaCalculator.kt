package com.tfm.rfidcartapp.tts

import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.model.Product


data class QuantityChange(val product: Product, val oldQty: Int, val newQty: Int)

data class CartDelta(
    val added: List<CartItem>,
    val removed: List<CartItem>,
    val increased: List<QuantityChange>,
    val decreased: List<QuantityChange>
) {
    fun isEmpty(): Boolean =
        added.isEmpty() && removed.isEmpty() && increased.isEmpty() && decreased.isEmpty()
}

object CartDeltaCalculator {

    fun compute(old: List<CartItem>, new: List<CartItem>): CartDelta {
        val oldMap = old.associateBy({ it.product.id }, { it.quantity })
        val newMap = new.associateBy({ it.product.id }, { it.quantity })

        val added = mutableListOf<CartItem>()
        val removed = mutableListOf<CartItem>()
        val increased = mutableListOf<QuantityChange>()
        val decreased = mutableListOf<QuantityChange>()

        val allIds = (oldMap.keys + newMap.keys).toSet()
        for (id in allIds) {
            val oldQty = oldMap[id] ?: 0
            val newQty = newMap[id] ?: 0
            if (oldQty == 0 && newQty > 0) {
                val product = new.first { it.product.id == id }.product
                added += CartItem(product, newQty)
            } else if (oldQty > 0 && newQty == 0) {
                val product = old.first { it.product.id == id }.product
                removed += CartItem(product, oldQty)
            } else if (oldQty > 0 && newQty > 0 && oldQty != newQty) {
                val product = (new.firstOrNull { it.product.id == id }?.product)
                    ?: old.first { it.product.id == id }.product
                if (newQty > oldQty) {
                    increased += QuantityChange(product, oldQty, newQty)
                } else {
                    decreased += QuantityChange(product, oldQty, newQty)
                }
            }
        }
        return CartDelta(added, removed, increased, decreased)
    }
}
