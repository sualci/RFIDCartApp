package com.tfm.rfidcartapp.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.data.model.Allergens
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.data.model.Product
import com.tfm.rfidcartapp.util.toPrice

val allProducts = listOf(
    Product("Whole Milk", "DairyBest", 1.20, setOf("leche")),
    Product("Wheat Bread", "Baker's", 1.50, setOf("gluten")),
    Product("Peanut Butter", "Nutty", 2.80, setOf("cacahuetes")),
    Product("Chocolate Bar", "SweetCo", 1.00, setOf("leche", "soja")),
    Product("Cheddar Cheese", "Cheesy", 2.50, setOf("leche")),
    Product("Almond Cookies", "CookieHouse", 3.00, setOf("frutos_cascara", "gluten")),
    Product("Orange Juice", "FreshSqueeze", 2.00, emptySet()),
    Product("Yogurt", "Creamy", 1.10, setOf("leche")),
    Product("Shrimp Pack", "SeaFoodies", 4.50, setOf("crustaceos")),
    Product("Egg Pasta", "PastaLover", 1.90, setOf("huevos", "gluten"))
)
val cartItems = listOf(
    CartItem(allProducts[0], quantity = 2), // 2 x Milk
    CartItem(allProducts[2], quantity = 1), // 1 x Peanut Butter
    CartItem(allProducts[4], quantity = 3), // 3 x Cheddar Cheese
    CartItem(allProducts[7], quantity = 2)  // 2 x Yogurt
)

enum class AllergenRisk { NONE, LOW, MEDIUM, HIGH }

data class RiskAssessment(
    val risk: AllergenRisk,
    val notableAllergenId: String? // the most notable matched allergen id (by priority)
)

// Lower number = higher priority (more "notable")
private val ALLERGEN_PRIORITY: Map<String, Int> = mapOf(
    "cacahuetes" to 0,
    "frutos_cascara" to 1,
    "crustaceos" to 2,
    "moluscos" to 3,
    "pescado" to 4,
    "huevos" to 5,
    "leche" to 6,
    "gluten" to 7,
    "soja" to 8,
    "sesamo" to 9,
    "apio" to 10,
    "mostaza" to 11,
    "altramuces" to 12,
    "sulfitos" to 13
)

private fun priorityOf(id: String): Int =
    ALLERGEN_PRIORITY[id] ?: Int.MAX_VALUE

fun assessRisk(productAllergens: Set<String>, userAllergens: Set<String>): RiskAssessment {
    val matches = productAllergens.intersect(userAllergens)
    if (matches.isEmpty()) return RiskAssessment(AllergenRisk.NONE, null)

    val notable = matches.minBy { priorityOf(it) }
    val risk = when (matches.size) {
        1 -> AllergenRisk.LOW
        2, 3 -> AllergenRisk.MEDIUM
        else -> AllergenRisk.HIGH
    }
    return RiskAssessment(risk, notable)
}

fun riskColor(risk: AllergenRisk): Color = when (risk) {
    AllergenRisk.NONE -> Color.Unspecified
    AllergenRisk.LOW -> Color(0xFFFFF9C4)    // light yellow
    AllergenRisk.MEDIUM -> Color(0xFFFFE0B2) // light orange
    AllergenRisk.HIGH -> Color(0xFFFFCDD2)   // light red
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    items: List<CartItem>,
    userAllergens: Set<String>
) {
    val totalPrice = items.sumOf { it.product.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de la compra") }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", fontWeight = FontWeight.Bold)
                    Text(totalPrice.toPrice(), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items) { item ->
                val p = item.product
                val lineTotal = p.price * item.quantity

                val assessment = assessRisk(p.allergens, userAllergens)
                val bg = riskColor(assessment.risk)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        // Title + quantity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                p.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("x${item.quantity}", style = MaterialTheme.typography.titleSmall)
                        }

                        Spacer(Modifier.height(4.dp))
                        Text("Brand: ${p.brand}", style = MaterialTheme.typography.bodySmall)

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Unit: ${p.price.toPrice()}", style = MaterialTheme.typography.bodyMedium)
                            Text("Subtotal: ${lineTotal.toPrice()}", fontWeight = FontWeight.SemiBold)
                        }

                        // Notable allergen chip + full list text
                        val notableId = assessment.notableAllergenId
                        if (p.allergens.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))

                            notableId?.let {
                                AssistChip(
                                    onClick = { /* no-op */ },
                                    label = { Text("Notable allergen: ${allergenLabel(it)}") },
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            Text(
                                "Allergens: ${p.allergens.joinToString(", ") { allergenLabel(it) }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
fun allergenLabel(id: String): String {
    val found = Allergens.all.firstOrNull { it.id == id }
    return found?.label ?: id
}

