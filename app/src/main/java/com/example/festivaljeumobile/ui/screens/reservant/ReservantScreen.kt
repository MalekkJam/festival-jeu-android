package com.example.festivaljeumobile.ui.screens.reservant

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.model.ReservantType
import com.example.festivaljeumobile.ui.screens.festival.StatusCard
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
import com.example.festivaljeumobile.viewModel.reservant.ReservantListUiState
import com.example.festivaljeumobile.viewModel.reservant.ReservantListViewModel

/**
 * Conteneur Screen principal pour la liste des réservants.
 * Responsable de l'injection du ViewModel et la communication avec AppNavHost.
 */
@Composable
fun ReservantScreen(
    onAddReservantClick: () -> Unit,
    onReservantClick: (Reservant) -> Unit,
    onEditReservantClick: (Reservant) -> Unit,
    canManageReservants: Boolean,
    modifier: Modifier = Modifier,
) {
    val reservantViewModel: ReservantListViewModel = viewModel()
    val uiState by reservantViewModel.uiState.collectAsState()

    ReservantList(
        uiState = uiState,
        onAddReservantClick = onAddReservantClick,
        onReservantClick = onReservantClick,
        onEditReservantClick = onEditReservantClick,
        onDeleteReservantClick = reservantViewModel::deleteReservant,
        onRetryClick = reservantViewModel::refreshReservants,
        canManageReservants = canManageReservants,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ReservantScreenPreview() {
    FestivalJeuMobileTheme {
        ReservantList(
            uiState = ReservantListUiState(
                reservants = listOf(
                    Reservant(
                        id = 1,
                        nom = "Dupont",
                        type = ReservantType.Editeur,
                        prenom = "Jean",
                        email = "jean.dupont@example.com",
                        telephone = "0601020304",
                        entreprise = "Acme Corp",
                        adresse = "123 Rue Exemple",
                        codePostal = "75001",
                        ville = "Paris"
                    )
                )
            ),
            onAddReservantClick = {},
            onReservantClick = {},
            onEditReservantClick = {},
            onDeleteReservantClick = {},
            onRetryClick = {},
            canManageReservants = true
        )
    }
}
