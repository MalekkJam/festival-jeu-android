package com.example.festivaljeumobile.ui.screens.jeu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.ui.screens.festival.StatusCard
import com.example.festivaljeumobile.ui.screens.jeu.components.JeuCard
import com.example.festivaljeumobile.viewModel.jeu.JeuListViewModel

/**
 * Écran liste des jeux
 * Crée son propre ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuListScreen(
    onJeuClick: (Int) -> Unit = {},
    onAddJeuClick: () -> Unit = {},
    onEditJeuClick: (Int) -> Unit = {},
    canManageGames: Boolean = true,
) {
    val viewModel: JeuListViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jeux") },
                actions = {
                    IconButton(onClick = { viewModel.refreshJeux() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rafraîchir")
                    }
                    IconButton(onClick = { viewModel.toggleSortDirection() }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Trier")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canManageGames && !state.isOffline) {
                FloatingActionButton(onClick = onAddJeuClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter un jeu")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isOffline) {
                StatusCard(
                    text = "Mode hors ligne : affichage du cache local.",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

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

            // Affichage des erreurs
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Indicateur de chargement
            if (state.isLoading && state.jeux.isEmpty()) {
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
                            onDeleteClick = { viewModel.deleteJeu(jeu.idJeu, jeu.libelleJeu) },
                            showActions = canManageGames && !state.isOffline
                        )
                    }
                }
            }
        }
    }
}
