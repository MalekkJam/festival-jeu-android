package com.example.festivaljeumobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.viewModel.auth.AuthEvent
import com.example.festivaljeumobile.viewModel.auth.AuthViewModel
import kotlinx.coroutines.launch
import com.example.festivaljeumobile.ui.screens.jeu.JeuListScreen
import com.example.festivaljeumobile.ui.screens.jeu.JeuFormScreen
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.repository.JeuRepositoryImpl
import com.example.festivaljeumobile.viewModel.jeu.JeuListViewModel
import com.example.festivaljeumobile.viewModel.jeu.JeuFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(isAdmin: Boolean = false) {
    val backStack = rememberNavBackStack(Login);
    val currentDestination = backStack.lastOrNull()
    val showNavBar = currentDestination != null && currentDestination !is Login

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val authViewModel: AuthViewModel = viewModel()
    val scope = rememberCoroutineScope()

    // Injecter les dépendances directement
    val jeuApi = remember { RetrofitInstance.retrofit.create(JeuApi::class.java) }
    val jeuRepository = remember { JeuRepositoryImpl(jeuApi) }

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
        gesturesEnabled = showNavBar,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                NavBarDestination.entries
                    .filter { !it.adminOnly || isAdmin }
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
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                    entry<Festivals> { Text("Festivals") }
                    entry<Jeux> {
                        val viewModel = remember { JeuListViewModel(jeuRepository) }
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
                        val viewModel = remember { JeuFormViewModel(jeuRepository) }
                        JeuFormScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                    entry<JeuEditForm> { jeuEditForm ->
                        val viewModel = remember { JeuFormViewModel(jeuRepository) }
                        JeuFormScreen(
                            jeuId = jeuEditForm.jeuId,
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                    entry<Reservations> { Text("Réservations") }
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