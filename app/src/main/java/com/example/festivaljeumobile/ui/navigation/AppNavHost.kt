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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole
import com.example.festivaljeumobile.ui.screens.admin.AdminScreen
import com.example.festivaljeumobile.ui.screens.auth.AuthScreen
import com.example.festivaljeumobile.ui.screens.festival.FestivalFormScreen
import com.example.festivaljeumobile.ui.screens.festival.FestivalScreen
import com.example.festivaljeumobile.ui.screens.jeu.JeuFormScreen
import com.example.festivaljeumobile.ui.screens.jeu.JeuListScreen
import com.example.festivaljeumobile.ui.screens.reservation.ReservationFormScreen
import com.example.festivaljeumobile.ui.screens.reservation.ReservationScreen
import com.example.festivaljeumobile.ui.screens.reservant.ReservantFormScreen
import com.example.festivaljeumobile.ui.screens.reservant.ReservantScreen
import com.example.festivaljeumobile.ui.screens.user.UserFormScreen
import com.example.festivaljeumobile.ui.screens.user.UserListScreen
import com.example.festivaljeumobile.viewModel.auth.AuthEvent
import com.example.festivaljeumobile.viewModel.auth.AuthViewModel
import com.example.festivaljeumobile.viewModel.jeu.JeuFormViewModel
import com.example.festivaljeumobile.viewModel.jeu.JeuListViewModel
import com.example.festivaljeumobile.viewModel.user.UserFormViewModel
import com.example.festivaljeumobile.viewModel.user.UserListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val context = LocalContext.current.applicationContext
    val app = context as FestivalApp
    var sessionRefreshTick by remember { mutableIntStateOf(0) }

    val startDestination by produceState<NavKey?>(initialValue = null, context) {
        value = withContext(Dispatchers.IO) {
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
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val backStack = rememberNavBackStack(startDestination!!)
    val currentDestination = backStack.lastOrNull()
    val currentUserRole by produceState<UserRole?>(
        initialValue = null,
        context,
        sessionRefreshTick,
    ) {
        value = withContext(Dispatchers.IO) {
            app.cookieDataStore.readUserRole()
        }
    }

    val showNavBar = currentDestination != null && currentDestination !is Login
    val isDetailDestination = currentDestination is FestivalForm ||
        currentDestination is FestivalDetails ||
        currentDestination is ReservationForm ||
        currentDestination is ReservantForm ||
        currentDestination is JeuForm ||
        currentDestination is JeuEditForm ||
        currentDestination is UserCreate ||
        currentDestination is UserEdit
    val showDrawer = showNavBar && !isDetailDestination

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val authViewModel = remember { AuthViewModel(app.authRepository) }
    val jeuListViewModel = remember { JeuListViewModel(app.jeuRepository) }
    val scope = rememberCoroutineScope()
    val isOffline = !context.isOnline()

    val canManageGames = currentUserRole in setOf(
        UserRole.Admin,
        UserRole.SuperOrganisateur,
        UserRole.Organisateur,
    )
    val canManageFestivals = currentUserRole in setOf(
        UserRole.Admin,
        UserRole.SuperOrganisateur,
    )
    val canManageReservations = currentUserRole in setOf(
        UserRole.Admin,
        UserRole.SuperOrganisateur,
    )
    val canManageReservants = currentUserRole in setOf(
        UserRole.Admin,
        UserRole.SuperOrganisateur,
    )

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> {
                    sessionRefreshTick++
                    backStack.clear()
                    backStack.add(Festivals)
                }

                is AuthEvent.NavigateToLogin -> {
                    sessionRefreshTick++
                    backStack.clear()
                    backStack.add(Login)
                }
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                )
                NavBarDestination.entries
                    .filter { destination ->
                        destination.isVisibleFor(currentUserRole) &&
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
                                } else {
                                    backStack.clear()
                                    backStack.add(destination.route)
                                }
                                scope.launch { drawerState.close() }
                            },
                        )
                    }
            }
        },
    ) {
        Scaffold(
            topBar = {
                if (showNavBar) {
                    TopAppBar(
                        title = { Text("Festival Jeu Mobile") },
                        navigationIcon = {
                            if (isDetailDestination) {
                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                                }
                            } else {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding),
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Login> {
                        AuthScreen(viewModel = authViewModel)
                    }

                    entry<Festivals> {
                        FestivalScreen(
                            onAddFestivalClick = { backStack.add(FestivalForm()) },
                            onFestivalClick = { festival ->
                                backStack.add(
                                    FestivalDetails(
                                        id = festival.id,
                                        nom = festival.nom,
                                        date_debut = festival.date_debut,
                                        date_fin = festival.date_fin,
                                        nbTables = festival.nbTables,
                                        zoneTarifaires = festival.zoneTarifaires,
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
                                        zoneTarifaires = festival.zoneTarifaires,
                                    )
                                )
                            },
                            canManageFestivals = canManageFestivals,
                        )
                    }

                    entry<FestivalForm> { festivalForm ->
                        FestivalFormScreen(
                            initialFestival = festivalForm.toFestivalOrNull(),
                            onBackClick = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<FestivalDetails> { festivalDetails ->
                        FestivalFormScreen(
                            initialFestival = festivalDetails.toFestivalOrNull(),
                            readOnly = true,
                            onBackClick = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<Reservations> {
                        ReservationScreen(
                            onAddReservationClick = {
                                backStack.add(ReservationForm())
                            },
                            onEditReservationClick = { reservation ->
                                backStack.add(ReservationForm(reservation = reservation))
                            },
                            canManageReservations = canManageReservations,
                        )
                    }

                    entry<ReservationForm> { reservationForm ->
                        ReservationFormScreen(
                            initialReservation = reservationForm.reservation,
                            onBackClick = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<Reservants> {
                        ReservantScreen(
                            onAddReservantClick = {
                                backStack.add(ReservantForm())
                            },
                            onReservantClick = { reservant ->
                                backStack.add(
                                    ReservantForm(
                                        id = reservant.id,
                                        nom = reservant.nom,
                                        type = reservant.type.name,
                                    )
                                )
                            },
                            onEditReservantClick = { reservant ->
                                backStack.add(
                                    ReservantForm(
                                        id = reservant.id,
                                        nom = reservant.nom,
                                        type = reservant.type.name,
                                    )
                                )
                            },
                            canManageReservants = canManageReservants,
                        )
                    }

                    entry<ReservantForm> { reservantForm ->
                        ReservantFormScreen(
                            initialReservant = reservantForm.toReservantOrNull(),
                            onBackClick = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<Benevoles> { Text("Benevoles") }
                    entry<Editeurs> { Text("Editeurs") }

                    entry<Jeux> {
                        JeuListScreen(
                            viewModel = jeuListViewModel,
                            onJeuClick = {},
                            onAddJeuClick = { backStack.add(JeuForm) },
                            onEditJeuClick = { jeuId -> backStack.add(JeuEditForm(jeuId)) },
                            canManageGames = canManageGames,
                        )
                    }

                    entry<JeuForm> {
                        val jeuFormViewModel = remember { JeuFormViewModel(app.jeuRepository) }
                        JeuFormScreen(
                            viewModel = jeuFormViewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                jeuListViewModel.refresh()
                                backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<JeuEditForm> { jeuEditForm ->
                        val jeuFormViewModel = remember { JeuFormViewModel(app.jeuRepository) }
                        JeuFormScreen(
                            jeuId = jeuEditForm.jeuId,
                            viewModel = jeuFormViewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onSuccessNavigateBack = {
                                jeuListViewModel.refresh()
                                backStack.removeLastOrNull()
                            },
                        )
                    }

                    entry<UserList> {
                        val userListViewModel = remember { UserListViewModel(app.userRepository) }
                        UserListScreen(
                            viewModel = userListViewModel,
                            onAddUserClick = { backStack.add(UserCreate) },
                            onEditUserClick = { user ->
                                backStack.add(
                                    UserEdit(
                                        id = user.id ?: return@UserListScreen,
                                        login = user.login,
                                        prenom = user.prenom ?: "",
                                        nom = user.nom ?: "",
                                        role = user.role,
                                    )
                                )
                            },
                        )
                    }

                    entry<UserCreate> {
                        val userFormViewModel = remember { UserFormViewModel(app.userRepository) }
                        UserFormScreen(
                            initialUser = null,
                            onBackClick = { backStack.removeLastOrNull() },
                            viewModel = userFormViewModel,
                        )
                    }

                    entry<UserEdit> { userEdit ->
                        val userFormViewModel = remember { UserFormViewModel(app.userRepository) }
                        UserFormScreen(
                            initialUser = userEdit.toUser(),
                            onBackClick = { backStack.removeLastOrNull() },
                            viewModel = userFormViewModel,
                        )
                    }

                    entry<Admin> {
                        AdminScreen(
                            onUsersClick = { backStack.add(UserList) },
                        )
                    }
                    entry<Logout> { Text("Logout") }
                },
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
            zoneTarifaires = zoneTarifaires,
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
            zoneTarifaires = zoneTarifaires,
        )
    }

private fun UserEdit.toUser() = User(
    id = id,
    login = login,
    prenom = prenom.ifBlank { null },
    nom = nom.ifBlank { null },
    role = role,
)

private fun ReservantForm.toReservantOrNull() =
    id?.let {
        com.example.festivaljeumobile.domain.model.Reservant(
            id = it,
            nom = nom,
            type = com.example.festivaljeumobile.domain.model.ReservantType.valueOf(type),
        )
    }

private fun NavBarDestination.isVisibleFor(role: UserRole?): Boolean =
    when (role) {
        UserRole.Admin -> true
        UserRole.SuperOrganisateur -> this != NavBarDestination.ADMIN
        UserRole.Organisateur -> this in setOf(
            NavBarDestination.FESTIVALS,
            NavBarDestination.JEUX,
            NavBarDestination.RESERVATIONS,
            NavBarDestination.RESERVANTS,
            NavBarDestination.LOGOUT,
        )
        else -> this in setOf(
            NavBarDestination.FESTIVALS,
            NavBarDestination.JEUX,
            NavBarDestination.RESERVATIONS,
            NavBarDestination.RESERVANTS,
            NavBarDestination.LOGOUT,
        )
    }
