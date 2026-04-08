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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.ui.theme.FestivalJeuMobileTheme
import com.example.festivaljeumobile.viewModel.festival.FestivalListUiState
import com.example.festivaljeumobile.viewModel.festival.FestivalListViewModel

@Composable
fun FestivalScreen(
    onAddFestivalClick: () -> Unit,
    onFestivalClick: (Festival) -> Unit,
    onEditFestivalClick: (Festival) -> Unit,
    canManageFestivals: Boolean,
    modifier: Modifier = Modifier,
) {
    val festivalViewModel: FestivalListViewModel = viewModel()
    val uiState by festivalViewModel.uiState.collectAsState()

    FestivalList(
        uiState = uiState,
        onAddFestivalClick = onAddFestivalClick,
        onFestivalClick = onFestivalClick,
        onEditFestivalClick = onEditFestivalClick,
        onDeleteFestivalClick = festivalViewModel::deleteFestival,
        onRetryClick = festivalViewModel::refreshFestivals,
        canManageFestivals = canManageFestivals,
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
private fun FestivalScreenPreview() {
    FestivalJeuMobileTheme {
        FestivalList(
            uiState = FestivalListUiState(
                festivals = listOf(
                    Festival(
                        id = 1L,
                        nom = "Festival du Jeu de Paris",
                        date_debut = "2026-05-12T00:00:00.000Z",
                        date_fin = "2026-05-15T00:00:00.000Z",
                        nbTables = 42
                    )
                )
            ),
            onAddFestivalClick = {},
            onFestivalClick = {},
            onEditFestivalClick = {},
            onDeleteFestivalClick = {},
            onRetryClick = {},
            canManageFestivals = true
        )
    }
}
