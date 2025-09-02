package com.tfm.rfidcartapp.data.model

// Clase de datos que representa un produto dispoñible na aplicación.
// Contén información básica necesaria para identificalo e xestionar alerxias.
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val price: Double,
    val allergens: Set<String> //allergen id
)

// Clase de datos que representa un ítem dentro do carro da compra.
// Relaciona un produto cunha cantidade concreta.
data class CartItem(
    val product: Product,
    val quantity: Int
)