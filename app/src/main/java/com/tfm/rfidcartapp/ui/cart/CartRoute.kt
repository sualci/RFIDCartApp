package com.tfm.rfidcartapp.ui.cart

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tfm.rfidcartapp.data.storage.SettingsRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

@Composable
fun CartRoute(
    repository: SettingsRepository = rememberRepository()
) {
    var userAllergens by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        repository.data
            .map { it.allergens }
            .collectLatest { userAllergens = it }
    }


    CartScreen(
        items = cartItems,
        userAllergens = userAllergens
    )
}

@Composable
private fun rememberRepository(): SettingsRepository {
    val ctx: Context = LocalContext.current
    return remember(ctx) { SettingsRepository(ctx) }
}
