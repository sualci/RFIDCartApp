package com.tfm.rfidcartapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.ui.main.MainActivity
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.ui.theme.RFIDCartAppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RFIDCartAppTheme {
                // Pantalla de login con botón para entrar
                LoginScreen(
                    onStartClick = {
                        // Al pulsar: abrir MainActivity y cerrar LoginActivity
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            putExtra("startDestination", "cart") // arranca en carrito
                        })
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onStartClick: () -> Unit) {
    // Pantalla simple con un botón centrado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2E9)) // color de fondo verde claro
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onStartClick) {
            Text(stringResource(id = R.string.btn_enter)) // texto del botón
        }
    }
}
