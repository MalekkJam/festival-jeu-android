package com.example.festivaljeumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.festivaljeumobile.ui.navigation.AppNavHost
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalJeuMobileTheme {
                AppNavHost()
            }
        }
    }
}