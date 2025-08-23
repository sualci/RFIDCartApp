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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.data.model.CartItem
import com.tfm.rfidcartapp.util.AllergenLabels
import com.tfm.rfidcartapp.util.toPrice

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
                title = { Text(stringResource(id = R.string.cart_title)) }
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
                    Text(stringResource(id = R.string.cart_total), fontWeight = FontWeight.Bold)
                    Text(totalPrice.toPrice(), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        val context = LocalContext.current
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
                        Text(
                            stringResource(id = R.string.product_brand) + ": ${p.brand}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(id = R.string.product_price) + ": ${p.price.toPrice()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                stringResource(id = R.string.product_subtotal) + ": ${lineTotal.toPrice()}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Notable allergen chip + full list text
                        val notableId = assessment.notableAllergenId
                        if (p.allergens.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))

                            notableId?.let {
                                AssistChip(
                                    onClick = { /* no-op */ },
                                    label = {
                                        Text(
                                            stringResource(id = R.string.product_allergens) + ": ${
                                                AllergenLabels.labelFor(context, it)
                                            }"
                                        )
                                    },
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            val allAllergens =
                                AllergenLabels.labelsFor(context, p.allergens).joinToString(", ")
                            Text(
                                text = stringResource(id = R.string.product_allergens_all) + ": " + allAllergens,
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

