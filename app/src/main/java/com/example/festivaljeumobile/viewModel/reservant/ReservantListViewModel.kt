package com.example.festivaljeumobile.viewModel.reservant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.data.repository.OfflineException
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.repository.ReservantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État UI pour la liste des réservants.
 * Représente tous les états possibles de la page de liste.
 */
data class ReservantListUiState(
    val reservants: List<Reservant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val deletingReservantId: Int? = null,
)

/**
 * ViewModel pour la gestion de la liste des réservants.
 * Orchestre le repository et expose un StateFlow<UiState> pour la UI.
 * Pattern : AndroidViewModel (accès Application via getApplication<>())
 */
class ReservantListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reservantRepository: ReservantRepository
        get() = (getApplication<Application>() as FestivalApp).reservantRepository

    private val _uiState = MutableStateFlow(ReservantListUiState())
    val uiState: StateFlow<ReservantListUiState> = _uiState.asStateFlow()

    private var observeReservantsJob: Job? = null

    /**
     * Initialise le ViewModel en chargeant les réservants.
     */
    init {
        loadReservants()
    }

    /**
     * Charge les réservants depuis le repository.
     * Établit un collecteur Flow qui tient la liste synchronisée.
     */
    fun loadReservants() {
        if (observeReservantsJob == null) {
            observeReservantsJob = viewModelScope.launch(Dispatchers.IO) {
                reservantRepository.getAll().collect { reservants ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            reservants = reservants,
                            error = currentState.error?.takeIf { reservants.isEmpty() }
                        )
                    }
                }
            }
        }
        refreshReservants()
    }

    /**
     * Supprime un réservant via le repository.
     * Gère l'état de suppression (loading) et les erreurs.
     */
    fun deleteReservant(reservant: Reservant) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    deletingReservantId = reservant.id,
                    error = null
                )
            }

            reservantRepository.delete(reservant).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(deletingReservantId = null)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            deletingReservantId = null,
                            error = throwable.message ?: "Impossible de supprimer le réservant."
                        )
                    }
                }
            )
        }
    }

    /**
     * Rafraîchit les réservants depuis l'API distante.
     * Gère l'état offline et les erreurs.
     */
    fun refreshReservants() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isOffline = false
                )
            }

            reservantRepository.refresh().fold(
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
                                offlineError && currentState.reservants.isNotEmpty() -> null
                                else -> throwable.message ?: "Erreur lors du chargement."
                            }
                        )
                    }
                }
            )
        }
    }
}
