package com.example.festivaldujeu.ui.screens.jeu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaldujeu.domain.model.Jeu
import com.example.festivaldujeu.domain.repository.JeuRepository
import com.example.festivaldujeu.ui.common.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour la liste des jeux
 * Respecte MVVM strict - aucune logique métier, uniquement orchestration
 * Expose un UiState observable via StateFlow
 * Dépend uniquement d'interfaces (JeuRepository, NetworkMonitor) - DIP
 */
@HiltViewModel
class JeuListViewModel @Inject constructor(
    private val jeuRepository: JeuRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // State privé (mutation interne)
    private val _uiState = MutableStateFlow(JeuListUiState())
    
    // State public (observable)
    val uiState: StateFlow<JeuListUiState> = _uiState.asStateFlow()

    init {
        // Combine les flux : jeux du repo + état réseau
        viewModelScope.launch {
            combine(
                jeuRepository.getAllJeux(),
                networkMonitor.isOnline,
                _uiState.map { it.searchQuery },
                _uiState.map { it.sortField },
                _uiState.map { it.sortDirection }
            ) { jeux, isOnline, _, _, _ ->
                _uiState.update { current ->
                    current.copy(
                        jeux = jeux,
                        isOffline = !isOnline,
                        isLoading = false
                    )
                }
            }.collect()
        }

        // Rafraîchissement initial
        refresh()
    }

    /**
     * Rafraîchit les jeux depuis l'API
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            jeuRepository.refreshJeux()
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    /**
     * Met à jour la requête de recherche
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

    /**
     * Supprime un jeu (déclenche navigation dans l'UI)
     */
    fun deleteJeu(idJeu: Int, libelleJeu: String) {
        viewModelScope.launch {
            jeuRepository.deleteJeu(idJeu, libelleJeu)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
