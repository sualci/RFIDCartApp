package com.tfm.rfidcartapp.data.model

data class Product(
    val name: String,
    val brand: String,
    val price: Double,
    val allergens: Set<String> //allergen id
)


data class CartItem(
   val product: Product,
   val quantity: Int
)