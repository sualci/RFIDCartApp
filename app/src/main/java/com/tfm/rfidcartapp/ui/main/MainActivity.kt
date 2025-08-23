package com.tfm.rfidcartapp.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tfm.rfidcartapp.ui.login.AppNav
import com.tfm.rfidcartapp.ui.theme.RFIDCartAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setOnExitAnimationListener { provider ->
            provider.iconView.animate().alpha(0f).setDuration(180L)
                .withEndAction { provider.remove() }.start()
        }

        setContent {
            RFIDCartAppTheme {
                AppNav() // hosts Navigation + conditional bottom bar
            }
        }
    }
}