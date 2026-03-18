package com.example.festivaljeumobile.ui.screens.jeu

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests unitaires pour JeuListViewModel
 * Pattern : test la logique, pas les Composables (voir Espresso pour UI)
 */
@RunWith(AndroidJUnit4::class)
class JeuListViewModelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // TODO: Tests à implémenter
    // - loadJeux() rafraîchit correctement
    // - recherche filtre correctement
    // - tri fonctionne
    // - état offline est respecté
}

/**
 * Tests Compose pour JeuListScreen
 * Pattern : test uniquement l'affichage et les événements UI
 */
class JeuListScreenTest {

    // TODO: Tests à implémenter avec Compose testing API
    // - Card cliquable navigue correctement
    // - Recherche met à jour le texte
    // - LoadingIndicator affiche pendant le chargement
}
