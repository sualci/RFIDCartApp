package com.tfm.rfidcartapp.tts

import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.model.Product


// Representa un cambio en la cantidad de un producto (antes y después)
data class QuantityChange(val product: Product, val oldQty: Int, val newQty: Int)

// Representa las diferencias entre dos estados del carrito (qué se añadió, quitó o cambió)
data class CartDelta(
    val added: List<CartItem>,        // Productos añadidos
    val removed: List<CartItem>,      // Productos eliminados
    val increased: List<QuantityChange>, // Productos con cantidad aumentada
    val decreased: List<QuantityChange>  // Productos con cantidad reducida
) {
    // Devuelve true si no hay ningún cambio en el carrito
    fun isEmpty(): Boolean =
        added.isEmpty() && removed.isEmpty() && increased.isEmpty() && decreased.isEmpty()
}

// Calcula las diferencias entre dos listas de items del carrito
object CartDeltaCalculator {

    // Compara dos carritos (estado viejo y nuevo) y devuelve los cambios como CartDelta
    fun compute(old: List<CartItem>, new: List<CartItem>): CartDelta {
        val oldMap = old.associateBy({ it.product.id }, { it.quantity }) // mapa id→cantidad del carrito anterior
        val newMap = new.associateBy({ it.product.id }, { it.quantity }) // mapa id→cantidad del carrito nuevo

        val added = mutableListOf<CartItem>()       // productos añadidos
        val removed = mutableListOf<CartItem>()     // productos eliminados
        val increased = mutableListOf<QuantityChange>() // cantidades aumentadas
        val decreased = mutableListOf<QuantityChange>() // cantidades reducidas

        val allIds = (oldMap.keys + newMap.keys).toSet() // conjunto de todos los ids presentes en viejo y nuevo
        for (id in allIds) {
            val oldQty = oldMap[id] ?: 0
            val newQty = newMap[id] ?: 0
            if (oldQty == 0 && newQty > 0) { // producto añadido
                val product = new.first { it.product.id == id }.product
                added += CartItem(product, newQty)
            } else if (oldQty > 0 && newQty == 0) { // producto eliminado
                val product = old.first { it.product.id == id }.product
                removed += CartItem(product, oldQty)
            } else if (oldQty > 0 && newQty > 0 && oldQty != newQty) { // cantidad modificada
                val product = (new.firstOrNull { it.product.id == id }?.product)
                    ?: old.first { it.product.id == id }.product
                if (newQty > oldQty) {
                    increased += QuantityChange(product, oldQty, newQty) // aumentó cantidad
                } else {
                    decreased += QuantityChange(product, oldQty, newQty) // redujo cantidad
                }
            }
        }
        return CartDelta(added, removed, increased, decreased) // resultado final
    }
}
