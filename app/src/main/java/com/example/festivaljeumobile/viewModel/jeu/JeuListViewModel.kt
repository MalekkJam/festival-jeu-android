package com.example.festivaljeumobile.viewModel.jeu

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.data.repository.OfflineException
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel pour la liste des jeux — Offline-First Architecture
 * Observe le Flow du repository qui est alimenté par Room
 * Pattern : Room → Flow → ViewModel observes → UI updates
 * Quand on supprime un jeu, le DAO le retire, Room notifie le Flow, et la liste se met à jour automatiquement
 */
class JeuListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val jeuRepository: JeuRepository
        get() = (getApplication<Application>() as FestivalApp).jeuRepository

    private val _uiState = MutableStateFlow(JeuListUiState())
    val uiState: StateFlow<JeuListUiState> = _uiState.asStateFlow()

    private var observejeuxJob: Job? = null

    init {
        loadJeux()
    }

    /**
     * Setup observation continue du repository Flow
     * Ce pattern garantit que l'UI se met à jour en temps réel quand Room change
     * Éxacutée une seule fois dans init
     */
    fun loadJeux() {
        if (observejeuxJob == null) {
            observejeuxJob = viewModelScope.launch(Dispatchers.IO) {
                Log.d("JeuListViewModel", "loadJeux() - Setting up continuous observation of repository Flow")
                jeuRepository.getAllJeux().collect { jeux ->
                    _uiState.update { currentState ->
                        Log.d("JeuListViewModel", "Flow emit: ${jeux.size} jeux from Room cache")
                        currentState.copy(
                            jeux = jeux,
                            error = currentState.error?.takeIf { jeux.isEmpty() }
                        )
                    }
                }
            }
        }
        // Première synchronisation depuis l'API
        refreshJeux()
    }

    /**
     * Rafraîchit les jeux depuis l'API et synchronise avec Room
     * Pattern:
     * 1. API fetch
     * 2. Room sync (upsertAll)
     * 3. Room notifie Flow
     * 4. ViewModel reçoit la notification
     * 5. UI se met à jour automatiquement
     */
    fun refreshJeux() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isOffline = false
                )
            }

            jeuRepository.refreshJeux().fold(
                onSuccess = {
                    Log.d("JeuListViewModel", "refreshJeux() success - waiting for Flow notification")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false
                        )
                    }
                },
                onFailure = { throwable ->
                    Log.e("JeuListViewModel", "refreshJeux() failed", throwable)
                    _uiState.update { currentState ->
                        val offlineError = throwable is OfflineException
                        currentState.copy(
                            isLoading = false,
                            isOffline = offlineError,
                            error = when {
                                offlineError && currentState.jeux.isNotEmpty() -> null
                                else -> throwable.message ?: "Impossible de récupérer les jeux."
                            }
                        )
                    }
                }
            )
        }
    }

    /**
     * Supprime un jeu via le repository
     * La suppression :
     * 1. Appelle l'API
     * 2. Appelle jeuDao.deleteById()
     * 3. Room notifie les observateurs du Flow
     * 4. Le Flow émet la liste mise à jour (sans le jeu supprimé)
     * 5. Cette nouvelle liste met à jour l'état UIState
     * 6. L'UI se met à jour automatiquement (compose observe uiState)
     */
    fun deleteJeu(idJeu: Int, libelleJeu: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    deletingJeuId = idJeu,
                    error = null
                )
            }

            jeuRepository.deleteJeu(idJeu, libelleJeu).fold(
                onSuccess = {
                    Log.d("JeuListViewModel", "deleteJeu($idJeu) success - waiting for Room to notify Flow")
                    _uiState.update {
                        it.copy(deletingJeuId = null)
                    }
                },
                onFailure = { throwable ->
                    Log.e("JeuListViewModel", "deleteJeu() failed", throwable)
                    _uiState.update {
                        it.copy(
                            deletingJeuId = null,
                            error = throwable.message ?: "Impossible de supprimer le jeu."
                        )
                    }
                }
            )
        }
    }

    /**
     * Met à jour la query de recherche
     */
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Change le champ de tri
     */
    fun setSortField(field: JeuSortField) {
        _uiState.update { it.copy(sortField = field) }
    }

    /**
     * Change la direction de tri
     */
    fun toggleSortDirection() {
        _uiState.update {
            it.copy(
                sortDirection = if (it.sortDirection == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
            )
        }
    }
}

/**
 * État UI pour la liste des jeux
 */
data class JeuListUiState(
    val jeux: List<Jeu> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val deletingJeuId: Int? = null,
    val sortField: JeuSortField = JeuSortField.NAME,
    val sortDirection: SortDirection = SortDirection.ASC
) {
    /**
     * Retourne les jeux filtrés et triés
     */
    val filteredJeux: List<Jeu>
        get() {
            var filtered = jeux
            
            // Filtre par recherche
            if (searchQuery.isNotEmpty()) {
                val query = searchQuery.lowercase()
                filtered = filtered.filter { jeu ->
                    jeu.libelleJeu.lowercase().contains(query) ||
                            (jeu.auteurJeu?.lowercase()?.contains(query) ?: false) ||
                            (jeu.theme?.lowercase()?.contains(query) ?: false) ||
                            (jeu.description?.lowercase()?.contains(query) ?: false)
                }
            }

            // Tri
            filtered = when (sortField) {
                JeuSortField.NAME -> filtered.sortedBy { it.libelleJeu }
                JeuSortField.AUTHOR -> filtered.sortedBy { it.auteurJeu }
                JeuSortField.DATE -> filtered.sortedBy { it.idJeu }
            }

            if (sortDirection == SortDirection.DESC) {
                filtered = filtered.reversed()
            }

            return filtered
        }
}

enum class JeuSortField {
    NAME, AUTHOR, DATE
}

enum class SortDirection {
    ASC, DESC
}
