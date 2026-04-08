package com.example.festivaljeumobile.ui.screens.reservation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.viewModel.reservation.ReservationListViewModel

@Composable
fun ReservationScreen(
    onAddReservationClick: () -> Unit,
    onEditReservationClick: (Reservation) -> Unit,
    canManageReservations: Boolean,
    modifier: Modifier = Modifier,
) {
    val reservationViewModel: ReservationListViewModel = viewModel()
    val uiState by reservationViewModel.uiState.collectAsState()

    ReservationList(
        uiState = uiState,
        onAddReservationClick = onAddReservationClick,
        onEditReservationClick = onEditReservationClick,
        onDeleteReservationClick = reservationViewModel::deleteReservation,
        onRetryClick = reservationViewModel::refreshReservations,
        canManageReservations = canManageReservations,
        modifier = modifier
    )
}
