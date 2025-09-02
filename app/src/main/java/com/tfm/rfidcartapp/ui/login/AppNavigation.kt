package com.tfm.rfidcartapp.ui.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.ui.cart.CartRoute
import com.yourpkg.ui.settings.SettingsRoute
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.ui.cart.CartViewModel

@Composable
fun AppNav(cartViewModel: CartViewModel) {
    val navController = rememberNavController() // controlador de navegación
    var isLoggedIn by rememberSaveable { mutableStateOf(false) } // estado de login

    val bottomBarHeight = 80.dp // altura reservada para la barra inferior

    // Cambia entre pantallas según el estado de login
    Crossfade(targetState = isLoggedIn, label = "auth-crossfade") { logged ->
        if (!logged) {
            // UI de login
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onStartClick = {
                            isLoggedIn = true // cambia a app al pulsar el botón
                        }
                    )
                    // Espacio reservado para la barra inferior futura
                    Spacer(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(bottomBarHeight)
                            .fillMaxWidth()
                    )
                }
            }
        } else {
            // UI principal de la app (carrito / ajustes)
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) } // barra inferior
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "cart", // pantalla inicial
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("cart") { CartRoute(cartViewModel = cartViewModel) } // ruta carrito
                    composable("settings") { SettingsRoute() } // ruta ajustes
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Definición de ítems de la barra inferior
    val items = listOf(
        BottomNavItem(stringResource(id = R.string.cart_title), "cart", Icons.Filled.ShoppingCart),
        BottomNavItem(
            stringResource(id = R.string.settings_title),
            "settings",
            Icons.Filled.Settings
        )
    )
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route, // resalta el ítem activo
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) } // icono del ítem
            )
        }
    }
}

// Datos de cada ítem de navegación inferior
data class BottomNavItem(val label: String, val route: String, val icon: ImageVector)
