package com.tfm.rfidcartapp.data.repository

import com.tfm.rfidcartapp.data.model.Product

val allProducts = listOf(
    Product("P-1001", "Leche entera", "Feiraco", 1.20, setOf("milk")),
    Product("P-1002", "Pan de molde", "Bimbo", 1.50, setOf("gluten")),
    Product("P-1003", "Crema de cacahuete", "Nutty", 2.80, setOf("peanuts")),
    Product("P-1004", "Tableta chocolate", "Milka", 1.00, setOf("milk", "soybean")),
    Product("P-1005", "Queso cheddar", "Charcuteria", 2.50, setOf("milk")),
    Product("P-1006", "Galletas de almendra", "CookieHouse", 3.00, setOf("treenuts", "gluten")),
    Product("P-1007", "Zumo de naranha", "Don Simón", 2.00, emptySet()),
    Product("P-1008", "Yogurt griego", "Danone", 1.10, setOf("milk")),
    Product("P-1009", "Gambas", "SeaFoodies", 4.50, setOf("crustaceans")),
    Product("P-1010", "Tallarines", "PastaR", 1.90, setOf("eggs", "gluten"))
)

val tagToProductId: Map<String, String> = mapOf(
    "E28069150000401D63E6F562" to "P-1001",
    "E28069150000401D63E6F962" to "P-1001",
    "E28069150000401D63E6ED62" to "P-1003",
    "E28069150000501D63E6E962" to "P-1004",
    "E28069150000401D63E6ED62" to "P-1005"
)

object ProductRepository {
    fun getById(id: String): Product? =
        allProducts.firstOrNull { it.id == id }
}
