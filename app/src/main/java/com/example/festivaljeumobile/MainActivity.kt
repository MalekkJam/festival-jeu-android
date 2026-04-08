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
        
        val authService = AuthService.getInstance()
        
        // Verify session at app startup
        lifecycleScope.launch {
            authService.verifySession()
        }
        
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
