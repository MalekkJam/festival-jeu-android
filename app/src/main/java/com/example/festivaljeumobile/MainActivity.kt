package com.example.festivaljeumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.festivaljeumobile.data.service.AuthService
import com.example.festivaljeumobile.ui.navigation.AppNavHost
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
import com.example.festivaljeumobile.viewModel.auth.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val authService = AuthService.getInstance()
        
        // Verify session at app startup
        lifecycleScope.launch {
            authService.verifySession()
        }
        
        setContent {
            FestivalJeuMobileTheme {
                AppContent(authService)
         }
     }
 }
}

@Composable
fun AppContent(authService: AuthService) {
    val isLoggedIn by authService.isLoggedIn.collectAsState(initial = false)
    
    when (isLoggedIn) {
        true -> {
            // User is logged in
            AppNavHost()
        }
        false -> {
            // User is not logged in
            val authViewModel: AuthViewModel = viewModel()
            AuthScreen(
                onLoginSuccess = { 
                    // After login, AuthService state is updated automatically
                    // The UI will recompose and show AppNavHost
                },
                viewModel = authViewModel
            )
        }
    }
}
