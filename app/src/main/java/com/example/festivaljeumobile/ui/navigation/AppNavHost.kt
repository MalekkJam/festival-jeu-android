package com.example.festivaljeumobile.ui.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.ui.screens.festival.FestivalFormScreen
import com.example.festivaljeumobile.ui.screens.festival.FestivalScreen
import com.example.festivaljeumobile.ui.screens.reservation.ReservationFormScreen
import com.example.festivaljeumobile.ui.screens.reservation.ReservationScreen
import com.example.festivaljeumobile.viewModel.auth.AuthEvent
import com.example.festivaljeumobile.viewModel.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.festivaljeumobile.di.ServiceLocator
import com.example.festivaljeumobile.ui.screens.jeu.JeuListScreen
import com.example.festivaljeumobile.ui.screens.jeu.JeuFormScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(isAdmin: Boolean = false) {
    val context = LocalContext.current.applicationContext
    val startDestination by produceState<NavKey?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) {
            val app = context as FestivalApp
            val hasSessionCookie = app.cookieDataStore.hasValidCookies()
            when {
                hasSessionCookie -> Festivals
                context.isOnline() -> Login
                app.festivalDatabase.festivalDao().hasFestivals() -> Festivals
                else -> Login
            }
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val backStack = rememberNavBackStack(startDestination!!);
    val currentDestination = backStack.lastOrNull()
    val showNavBar = currentDestination != null && currentDestination !is Login
    val showDrawer = currentDestination != null &&
        currentDestination !is Login &&
        currentDestination !is FestivalForm &&
        currentDestination !is FestivalDetails &&
        currentDestination !is ReservationForm

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val authViewModel: AuthViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val isOffline = !context.isOnline()

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> {
                    backStack.clear()
                    backStack.add(Festivals)
                }
                // Manage the logout call
                is AuthEvent.NavigateToLogin -> {
                    backStack.clear()
                    backStack.add(Login)
                }
                else -> {}
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                NavBarDestination.entries
                    .filter { destination ->
                        (!destination.adminOnly || isAdmin) &&
                            (!isOffline || destination != NavBarDestination.LOGOUT)
                    }
                    .forEach { destination ->
                        NavigationDrawerItem(
                            label = { Text(destination.label) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            selected = currentDestination == destination.route,
                            onClick = {
                                if (destination == NavBarDestination.LOGOUT) {
                                    authViewModel.logout()
                                }
                                backStack.clear()
                                backStack.add(destination.route)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (showNavBar) {
                    TopAppBar(
                        title = { Text("Festival Jeu Mobile") },
                        navigationIcon = {
                            if (
                                currentDestination is FestivalForm ||
                                currentDestination is FestivalDetails ||
                                currentDestination is ReservationForm
                            ) {
                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Login> { AuthScreen(viewModel = authViewModel)}
                    entry<Festivals> {
                        FestivalScreen(
                            onAddFestivalClick = {
                                backStack.add(FestivalForm())
                            },
                            onFestivalClick = { festival ->
                                backStack.add(
                                    FestivalDetails(
                                        id = festival.id,
                                        nom = festival.nom,
                                        date_debut = festival.date_debut,
                                        date_fin = festival.date_fin,
                                        nbTables = festival.nbTables,
                                        zoneTarifaires = festival.zoneTarifaires
                                    )
                                )
                            },
                            onEditFestivalClick = { festival ->
                                backStack.add(
                                    FestivalForm(
                                        id = festival.id,
                                        nom = festival.nom,
                                        date_debut = festival.date_debut,
                                        date_fin = festival.date_fin,
                                        nbTables = festival.nbTables,
                                        zoneTarifaires = festival.zoneTarifaires
                                    )
                                )
                            }
                        )
                    }
                    entry<FestivalForm> { festivalForm ->
                        FestivalFormScreen(
                            initialFestival = festivalForm.toFestivalOrNull(),
                            onBackClick = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<FestivalDetails> { festivalDetails ->
                        FestivalFormScreen(
                            initialFestival = festivalDetails.toFestivalOrNull(),
                            readOnly = true,
                            onBackClick = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Reservations> {
                        ReservationScreen(
                            onAddReservationClick = {
                                backStack.add(ReservationForm())
                            },
                            onEditReservationClick = { reservation ->
                                backStack.add(ReservationForm(reservation = reservation))
                            }
                        )
                    }
                    entry<ReservationForm> { reservationForm ->
                        ReservationFormScreen(
                            initialReservation = reservationForm.reservation,
                            onBackClick = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<Jeux> {
                        val viewModel = remember { ServiceLocator.createJeuListViewModel() }
                        JeuListScreen(
                            viewModel = viewModel,
                            onJeuClick = { jeuId ->
                                // À implémenter si JeuDetailScreen existe
                            },
                            onAddJeuClick = {
                                backStack.add(JeuForm)
                            },
                            onEditJeuClick = { jeuId ->
                                backStack.add(JeuEditForm(jeuId))
                            }
                        )
                    }
                    entry<JeuForm> {
                        val viewModel = remember { ServiceLocator.createJeuFormViewModel() }
                        JeuFormScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                    entry<JeuEditForm> { jeuEditForm ->
                        val viewModel = remember { ServiceLocator.createJeuFormViewModel() }
                        JeuFormScreen(
                            jeuId = jeuEditForm.jeuId,
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                    entry<Benevoles> { Text("Bénévoles") }
                    entry<Editeurs> { Text("Éditeurs") }
                    entry<Reservants> { Text("Réservants") }
                    entry<Admin> { Text("Admin") }
                    entry <Logout> { Text("Logout") }
                }
            )
        }
    }
}

private fun Context.isOnline(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun FestivalForm.toFestivalOrNull() =
    id?.let {
        com.example.festivaljeumobile.domain.model.Festival(
            id = it,
            nom = nom,
            date_debut = date_debut,
            date_fin = date_fin,
            nbTables = nbTables,
            zoneTarifaires = zoneTarifaires
        )
    }

private fun FestivalDetails.toFestivalOrNull() =
    id?.let {
        com.example.festivaljeumobile.domain.model.Festival(
            id = it,
            nom = nom,
            date_debut = date_debut,
            date_fin = date_fin,
            nbTables = nbTables,
            zoneTarifaires = zoneTarifaires
        )
    }
