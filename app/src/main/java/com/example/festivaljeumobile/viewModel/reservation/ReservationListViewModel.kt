package com.example.festivaljeumobile.viewModel.reservation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.data.repository.OfflineException
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.repository.ReservationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationListUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val deletingReservationId: Long? = null,
)

class ReservationListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reservationRepository: ReservationRepository
        get() = (getApplication<Application>() as FestivalApp).reservationRepository

    private val _uiState = MutableStateFlow(ReservationListUiState())
    val uiState: StateFlow<ReservationListUiState> = _uiState.asStateFlow()

    private var observeReservationsJob: Job? = null

    init {
        loadReservations()
    }

    fun loadReservations() {
        if (observeReservationsJob == null) {
            observeReservationsJob = viewModelScope.launch(Dispatchers.IO) {
                reservationRepository.getAll().collect { reservations ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            reservations = reservations,
                            error = currentState.error?.takeIf { reservations.isEmpty() }
                        )
                    }
                }
            }
        }
        refreshReservations()
    }

    fun refreshReservations() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isOffline = false
                )
            }

            reservationRepository.refresh().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { currentState ->
                        val offlineError = throwable is OfflineException
                        currentState.copy(
                            isLoading = false,
                            isOffline = offlineError,
                            error = when {
                                offlineError && currentState.reservations.isNotEmpty() -> null
                                else -> throwable.message ?: "Impossible de recuperer les reservations."
                            }
                        )
                    }
                }
            )
        }
    }

    fun deleteReservation(reservationId: Long?) {
        if (reservationId == null || reservationId <= 0L) {
            _uiState.update {
                it.copy(error = "Identifiant de reservation manquant.")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    deletingReservationId = reservationId,
                    error = null
                )
            }

            reservationRepository.delete(reservationId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(deletingReservationId = null)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            deletingReservationId = null,
                            error = throwable.message ?: "Impossible de supprimer la reservation."
                        )
                    }
                }
            )
        }
    }
}
