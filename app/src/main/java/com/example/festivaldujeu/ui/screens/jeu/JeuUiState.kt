package com.example.festivaldujeu.ui.screens.jeu

import com.example.festivaldujeu.domain.model.Jeu

/**
 * UiState pour la liste des jeux
 * Pattern data class : état continu observable
 * Suit les conventions du README (émis via StateFlow)
 */
data class JeuListUiState(
    val jeux: List<Jeu> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val sortField: JeuSortField = JeuSortField.LibelleJeu,
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
                JeuSortField.LibelleJeu -> filtered.sortedBy { it.libelleJeu }
                JeuSortField.AuteurJeu -> filtered.sortedBy { it.auteurJeu }
                JeuSortField.NbMinJoueurJeu -> filtered.sortedBy { it.nbMinJoueurJeu }
                JeuSortField.NbMaxJoueurJeu -> filtered.sortedBy { it.nbMaxJoueurJeu }
                JeuSortField.Agemini -> filtered.sortedBy { it.agemini }
                JeuSortField.Duree -> filtered.sortedBy { it.duree }
                JeuSortField.Theme -> filtered.sortedBy { it.theme }
                JeuSortField.Description -> filtered.sortedBy { it.description }
                else -> filtered.sortedBy { it.libelleJeu }
            }

            if (sortDirection == SortDirection.DESC) {
                filtered = filtered.reversed()
            }

            return filtered
        }
}

enum class JeuSortField {
    LibelleJeu,
    AuteurJeu,
    NbMinJoueurJeu,
    NbMaxJoueurJeu,
    NoticeJeu,
    IdEditeur,
    IdTypeJeu,
    Agemini,
    Prototype,
    Duree,
    Theme,
    Description,
    ImageJeu,
    VideoRegle
}

enum class SortDirection {
    ASC, DESC
}

/**
 * UiState pour le formulaire/détail d'un jeu
 * Pattern sealed class : états discrets
 */
sealed class JeuDetailUiState {
    object Loading : JeuDetailUiState()
    data class Success(val jeu: Jeu) : JeuDetailUiState()
    data class Error(val message: String) : JeuDetailUiState()
    object NotFound : JeuDetailUiState()
}

/**
 * UiState pour les actions (création/modification/suppression)
 * Pattern sealed class : événements one-shot
 */
sealed class JeuActionUiState {
    object Idle : JeuActionUiState()
    object Loading : JeuActionUiState()
    data class Success(val message: String) : JeuActionUiState()
    data class Error(val message: String) : JeuActionUiState()
}
