package com.example.festivaljeumobile.viewModel.jeu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel pour la liste des jeux
 * Respecte MVVM strict - aucune logique métier, orchestration uniquement
 */
class JeuListViewModel(
    private val jeuRepository: JeuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JeuListUiState())
    val uiState: StateFlow<JeuListUiState> = _uiState.asStateFlow()

    init {
        loadJeux()
    }

    /**
     * Charge les jeux depuis le repository
     */
    fun loadJeux() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            jeuRepository.getAllJeux().collect { jeux ->
                _uiState.update {
                    it.copy(
                        jeux = jeux,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Rafraîchit les jeux depuis l'API
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            jeuRepository.refreshJeux()
                .onSuccess {
                    loadJeux()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
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
     * Supprime un jeu
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

/**
 * État UI pour la liste des jeux
 */
data class JeuListUiState(
    val jeux: List<Jeu> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
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
