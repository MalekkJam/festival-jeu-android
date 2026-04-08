package com.example.festivaljeumobile.ui.screens.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.ui.screens.festival.StatusCard
import com.example.festivaljeumobile.viewModel.reservation.ReservationListUiState

@Composable
fun ReservationList(
    uiState: ReservationListUiState,
    onAddReservationClick: () -> Unit,
    onEditReservationClick: (Reservation) -> Unit,
    onDeleteReservationClick: (Long?) -> Unit,
    onRetryClick: () -> Unit,
    canManageReservations: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (canManageReservations && !uiState.isOffline) {
            Button(
                onClick = onAddReservationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Ajouter une reservation",
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
        }

        if (uiState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
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
        }

        when {
            uiState.isLoading && uiState.reservations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.reservations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune reservation disponible",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.reservations,
                        key = { reservation -> reservation.id ?: reservation.hashCode().toLong() }
                    ) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            canUpdate = canManageReservations && !uiState.isOffline,
                            isDeleting = uiState.deletingReservationId == reservation.id,
                            onUpdateClick = { onEditReservationClick(reservation) },
                            onDeleteClick = { onDeleteReservationClick(reservation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    canUpdate: Boolean,
    isDeleting: Boolean,
    onUpdateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reservation.reservantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (canUpdate) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onUpdateClick, enabled = !isDeleting) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la reservation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer la reservation",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            ReservationInfoRow(label = "Reservation", value = "#${reservation.id ?: "-"}")
            ReservationInfoRow(label = "Festival", value = reservation.festivalName)
            ReservationInfoRow(label = "Etat", value = reservation.etatDeSuivi)
            ReservationInfoRow(
                label = "Date de contact",
                value = reservation.dateDeContact?.toDisplayDate() ?: "Aucune"
            )
            ReservationInfoRow(
                label = "Remise",
                value = reservation.remise?.let { remise ->
                    val montant = reservation.montantRemise?.toString() ?: "-"
                    "$remise ($montant)"
                } ?: "Aucune"
            )
            ReservationInfoRow(label = "Facturee", value = reservation.facturee.toOuiNon())
            ReservationInfoRow(label = "Payee", value = reservation.payee.toOuiNon())
            ReservationInfoRow(label = "Jeux reserves", value = reservation.jeux.size.toString())
        }
    }
}

@Composable
private fun ReservationInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun String.toDisplayDate(): String = substringBefore("T")
private fun Boolean.toOuiNon(): String = if (this) "Oui" else "Non"
