package com.tfm.rfidcartapp.data.repository

import com.tfm.rfidcartapp.data.model.Product


// Lista estática con todos los productos disponibles en la aplicación.
// Cada producto incluye su id, nombre, marca, precio y posibles alérgenos.
val allProducts = listOf(
    Product("P-1001", "Leche entera", "Feiraco", 1.20, setOf("milk")),                  // Producto con alérgeno: leche
    Product("P-1002", "Pan de molde", "Bimbo", 1.50, setOf("gluten")),                  // Producto con alérgeno: gluten
    Product("P-1003", "Crema de cacahuete", "Nutty", 2.80, setOf("peanuts")),           // Producto con alérgeno: cacahuetes
    Product("P-1004", "Tableta chocolate", "Milka", 1.00, setOf("milk", "soybean")),    // Producto con leche y soja
    Product("P-1005", "Queso cheddar", "Charcuteria", 2.50, setOf("milk")),             // Producto con alérgeno: leche
    Product("P-1006", "Galletas de almendra", "CookieHouse", 3.00, setOf("treenuts", "gluten")), // Frutos secos + gluten
    Product("P-1007", "Zumo de naranha", "Don Simón", 2.00, emptySet()),                // Producto sin alérgenos
    Product("P-1008", "Yogurt griego", "Danone", 1.10, setOf("milk")),                  // Producto con leche
    Product("P-1009", "Gambas", "SeaFoodies", 4.50, setOf("crustaceans")),              // Producto con crustáceos
    Product("P-1010", "Tallarines", "PastaR", 1.90, setOf("eggs", "gluten"))            // Producto con huevo y gluten
)

// Mapa que relaciona IDs de etiquetas RFID (EPC) con los IDs internos de productos.
// Permite saber qué producto corresponde a cada etiqueta detectada por el lector.
val tagToProductId: Map<String, String> = mapOf(
    "E28069150000401D63E6F562" to "P-1001", // Etiqueta → Leche entera
    "E28069150000401D63E6F962" to "P-1001", // Otra etiqueta distinta que también corresponde a la leche
    "E28069150000401D63E6ED62" to "P-1003", // Etiqueta → Crema de cacahuete
    "E28069150000501D63E6E962" to "P-1004", // Etiqueta → Tableta chocolate
    "E28069150000401D63E6ED62" to "P-1005"  // ¡Ojo! Esta etiqueta está repetida con P-1003 (posible error o colisión)
)

// Objeto repositorio que permite acceder a los productos.
// Funciona como capa intermedia para consultar por ID.
object ProductRepository {
    // Devuelve un producto dado su id, o null si no se encuentra
    fun getById(id: String): Product? =
        allProducts.firstOrNull { it.id == id }
}
