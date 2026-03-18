package com.example.festivaljeumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
import com.example.festivaljeumobile.viewModel.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FestivalJeuMobileTheme {
                val authViewModel : AuthViewModel = viewModel()
                AuthScreen({}, authViewModel);
            }
        }
    }
}
