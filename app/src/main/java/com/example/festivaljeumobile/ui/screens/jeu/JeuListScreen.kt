package com.example.festivaldujeu.ui.screens.jeu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.festivaldujeu.ui.screens.jeu.components.JeuCard

/**
 * Écran liste des jeux (JeuList destination)
 * Pattern : aucune logique métier, uniquement affichage + collecte d'événements
 * Suit Material Design 3 exclusivement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuListScreen(
    viewModel: JeuListViewModel = hiltViewModel(),
    onJeuClick: (Int) -> Unit = {},
    onAddJeuClick: () -> Unit = {},
    onEditJeuClick: (Int) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value as JeuListUiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Rechercher")
                    }
                    IconButton(onClick = { viewModel.toggleSortDirection() }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Trier")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJeuClick) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un jeu")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Rechercher...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Indicateur offline
            if (state.isOffline) {
                ErrorBanner(message = "Mode hors-ligne")
            }

            // Indicateur de chargement
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredJeux.isEmpty()) {
                // État vide
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Aucun jeu trouvé", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // Grille de jeux (3 colonnes)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        state.filteredJeux,
                        key = { jeu -> jeu.idJeu }
                    ) { jeu ->
                        JeuCard(
                            jeu = jeu,
                            onCardClick = { onJeuClick(jeu.idJeu) },
                            onEditClick = { onEditJeuClick(jeu.idJeu) },
                            onDeleteClick = { viewModel.deleteJeu(jeu.idJeu, jeu.libelleJeu) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable réutilisable pour afficher une bannière d'erreur/offline
 */
@Composable
fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
