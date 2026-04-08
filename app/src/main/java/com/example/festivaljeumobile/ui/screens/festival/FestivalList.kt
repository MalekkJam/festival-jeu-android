package com.example.festivaljeumobile.ui.screens.festival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.viewModel.festival.FestivalListUiState

@Composable
fun FestivalList(
    uiState: FestivalListUiState,
    onAddFestivalClick: () -> Unit,
    onFestivalClick: (Festival) -> Unit,
    onEditFestivalClick: (Festival) -> Unit,
    onDeleteFestivalClick: (Festival) -> Unit,
    onRetryClick: () -> Unit,
    canManageFestivals: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (canManageFestivals && !uiState.isOffline) {
            Button(
                onClick = onAddFestivalClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Ajouter un festival",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isOffline) {
            StatusCard(
                text = "Mode hors ligne : affichage du cache local.",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = onRetryClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reessayer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.isLoading && uiState.festivals.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                Text(
                    text = "Mise a jour des festivals...",
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoading && uiState.festivals.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.festivals.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun festival disponible",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.festivals,
                        key = { festival -> festival.id }
                    ) { festival ->
                        FestivalCard(
                            festival = festival,
                            canUpdate = canManageFestivals && !uiState.isOffline,
                            canDelete = canManageFestivals && !uiState.isOffline,
                            isDeleting = uiState.deletingFestivalId == festival.id,
                            onClick = { onFestivalClick(festival) },
                            onUpdateClick = { onEditFestivalClick(festival) },
                            onDeleteClick = { onDeleteFestivalClick(festival) }
                        )
                    }
                }
            }
        }
    }
}
