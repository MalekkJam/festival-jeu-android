package com.example.festivaljeumobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableListOf
import androidx.compose.runtime.remember
import com.example.festivaljeumobile.ui.screens.jeu.JeuFormScreen
import com.example.festivaljeumobile.ui.screens.jeu.JeuListScreen

/**
 * Navigation Graph pour l'application
 * Utilise Navigation 3 avec destinations typées (@Serializable)
 *
 * IMPORTANT : Navigation 3 diffère de Navigation 2/NavController :
 * - Pas de NavHost, pas de NavController
 * - Le backstack est manuellement géré via SnapshotStateList<Any>
 * - L'affichage se fait via NavDisplay { entry<Destination> { ... } }
 * - Les destinations sont des objets @Serializable, pas des routes String
 */

@Composable
fun AppNavGraph(
    onNavigate: (AppDestination) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Exemple : navigation vers la liste des jeux
    // Peut être appelée depuis n'importe quel composable
}

/**
 * Exemple d'utilisation dans une composable (pseudo-code)
 * 
 * @Composable
 * fun AppScreen() {
 *     val backStack = remember { mutableListOf<AppDestination>(JeuList) }
 *     val currentDestination = backStack.lastOrNull() as? AppDestination
 * 
 *     NavDisplay(
 *         backStack = backStack,
 *         modifier = Modifier.fillMaxSize()
 *     ) { destination ->
 *         when (destination) {
 *             is JeuList -> JeuListScreen(
 *                 onJeuClick = { id ->
 *                     backStack.add(JeuDetail(id))
 *                 },
 *                 onAddJeuClick = {
 *                     backStack.add(JeuForm)
 *                 },
 *                 onEditJeuClick = { id ->
 *                     backStack.add(JeuEditForm(id))
 *                 }
 *             )
 *             is JeuDetail -> JeuDetailScreen(
 *                 jeuId = destination.jeuId,
 *                 onNavigateBack = { backStack.removeAt(backStack.lastIndex) }
 *             )
 *             is JeuForm -> JeuFormScreen(
 *                 onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
 *                 onSuccessNavigateBack = { 
 *                     backStack.clear()
 *                     backStack.add(JeuList)
 *                 }
 *             )
 *             is JeuEditForm -> JeuFormScreen(
 *                 jeuId = destination.jeuId,
 *                 onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
 *                 onSuccessNavigateBack = { 
 *                     backStack.removeAt(backStack.lastIndex)
 *                 }
 *             )
 *             else -> { // Unknown destination // }
 *         }
 *     }
 * }
 */
