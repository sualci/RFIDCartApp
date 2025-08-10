package com.tfm.rfidcartapp.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tfm.rfidcartapp.MainActivity
import com.tfm.rfidcartapp.R
import com.tfm.rfidcartapp.ui.theme.RFIDCartAppTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RFIDCartAppTheme {
                SplashScreen(
                    onStartClick = {
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            putExtra("startDestination", "settings")
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onStartClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2E9))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.shop_cart),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onStartClick) {
                Text(stringResource(id = R.string.btn_enter))
            }
        }
    }
}
