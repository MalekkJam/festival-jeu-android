package com.example.festivaljeumobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

enum class NavBarDestination(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
) {
    FESTIVALS(Festivals, "Festivals", Icons.Default.Festival),
    JEUX(Jeux, "Jeux", Icons.Default.SportsEsports),
    RESERVATIONS(Reservations, "Réservations", Icons.Default.ConfirmationNumber),
    BENEVOLES(Benevoles, "Bénévoles", Icons.Default.People),
    EDITEURS(Editeurs, "Éditeurs", Icons.Default.Business),
    RESERVANTS(Reservants, "Réservants", Icons.Default.Person),
    ADMIN(Admin, "Admin", Icons.Default.AdminPanelSettings, adminOnly = true),
    LOGOUT(Logout, "Logout", Icons.Default.Logout)
}