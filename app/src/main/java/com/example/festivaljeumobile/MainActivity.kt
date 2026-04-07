package com.example.festivaljeumobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.festivaljeumobile.data.service.AuthService
import com.example.festivaljeumobile.ui.navigation.AppNavHost
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
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
            AuthScreen(
                onLoginSuccess = { 
                    // After login, AuthService state is updated automatically
                    // The UI will recompose and show AppNavHost
                }
            )
        }
    }
}
