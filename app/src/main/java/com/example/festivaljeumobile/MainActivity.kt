package com.example.festivaljeumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.ui.navigation.AppNavHost
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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

    override fun onDestroy() {
        if (isFinishing && !isChangingConfigurations) {
            runBlocking(Dispatchers.IO) {
                RetrofitInstance.clearCookies()
            }
        }
        super.onDestroy()
    }
}
