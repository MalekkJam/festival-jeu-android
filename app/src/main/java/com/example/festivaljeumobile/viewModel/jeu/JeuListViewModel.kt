package com.example.festivaljeumobile.viewModel.jeu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.data.repository.OfflineException
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JeuListViewModel(
    private val jeuRepository: JeuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JeuListUiState())
    val uiState: StateFlow<JeuListUiState> = _uiState.asStateFlow()
    private var loadJeuxJob: Job? = null

    init {
        loadJeux()
    }

    fun loadJeux() {
        loadJeuxJob?.cancel()
        loadJeuxJob = viewModelScope.launch {
            jeuRepository.getAllJeux().collect { jeux ->
                _uiState.update { current ->
                    current.copy(
                        jeux = jeux,
                        isLoading = false,
                        error = current.error?.takeIf { jeux.isEmpty() }
                    )
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isOffline = false
                )
            }
            jeuRepository.refreshJeux()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        val offlineError = error is OfflineException
                        current.copy(
                            isLoading = false,
                            isOffline = offlineError,
                            error = when {
                                offlineError && current.jeux.isNotEmpty() -> null
                                else -> error.message ?: "Impossible de recuperer les jeux."
                            }
                        )
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSortField(field: JeuSortField) {
        _uiState.update { it.copy(sortField = field) }
    }

    fun toggleSortDirection() {
        _uiState.update {
            it.copy(
                sortDirection = if (it.sortDirection == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC
            )
        }
    }

    fun deleteJeu(idJeu: Int, libelleJeu: String) {
        viewModelScope.launch {
            jeuRepository.deleteJeu(idJeu, libelleJeu)
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(
                            jeux = current.jeux.filterNot { it.idJeu == idJeu },
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}

data class JeuListUiState(
    val jeux: List<Jeu> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val sortField: JeuSortField = JeuSortField.NAME,
    val sortDirection: SortDirection = SortDirection.ASC
) {
    val filteredJeux: List<Jeu>
        get() {
            var filtered = jeux

            if (searchQuery.isNotEmpty()) {
                val query = searchQuery.lowercase()
                filtered = filtered.filter { jeu ->
                    jeu.libelleJeu.lowercase().contains(query) ||
                        (jeu.auteurJeu?.lowercase()?.contains(query) ?: false) ||
                        (jeu.theme?.lowercase()?.contains(query) ?: false) ||
                        (jeu.description?.lowercase()?.contains(query) ?: false)
                }
            }

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
