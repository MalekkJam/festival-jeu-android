package com.example.festivaldujeu.ui.screens.jeu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaldujeu.domain.model.Jeu
import com.example.festivaldujeu.domain.repository.JeuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour le détail d'un jeu et la gestion des actions (add/update)
 */
@HiltViewModel
class JeuFormViewModel @Inject constructor(
    private val jeuRepository: JeuRepository
) : ViewModel() {

    private val _detailUiState = MutableStateFlow<JeuDetailUiState>(JeuDetailUiState.Loading)
    val detailUiState: StateFlow<JeuDetailUiState> = _detailUiState.asStateFlow()

    private val _actionUiState = MutableStateFlow<JeuActionUiState>(JeuActionUiState.Idle)
    val actionUiState: StateFlow<JeuActionUiState> = _actionUiState.asStateFlow()

    /**
     * Charge un jeu par ID
     */
    fun loadJeu(idJeu: Int) {
        viewModelScope.launch {
            _detailUiState.value = JeuDetailUiState.Loading
            jeuRepository.getJeuById(idJeu)
                .onSuccess { jeu ->
                    if (jeu != null) {
                        _detailUiState.value = JeuDetailUiState.Success(jeu)
                    } else {
                        _detailUiState.value = JeuDetailUiState.NotFound
                    }
                }
                .onFailure { e ->
                    _detailUiState.value = JeuDetailUiState.Error(e.message ?: "Erreur inconnue")
                }
        }
    }

    /**
     * Crée un nouveau jeu
     */
    fun addJeu(jeu: Jeu) {
        viewModelScope.launch {
            _actionUiState.value = JeuActionUiState.Loading
            jeuRepository.addJeu(jeu)
                .onSuccess {
                    _actionUiState.value = JeuActionUiState.Success("Jeu créé avec succès")
                }
                .onFailure { e ->
                    _actionUiState.value = JeuActionUiState.Error(e.message ?: "Erreur lors de la création")
                }
        }
    }

    /**
     * Met à jour un jeu existant
     */
    fun updateJeu(jeu: Jeu) {
        viewModelScope.launch {
            _actionUiState.value = JeuActionUiState.Loading
            jeuRepository.updateJeu(jeu)
                .onSuccess {
                    _actionUiState.value = JeuActionUiState.Success("Jeu mis à jour avec succès")
                }
                .onFailure { e ->
                    _actionUiState.value = JeuActionUiState.Error(e.message ?: "Erreur lors de la mise à jour")
                }
        }
    }

    /**
     * Supprime un jeu
     */
    fun deleteJeu(idJeu: Int, libelleJeu: String) {
        viewModelScope.launch {
            _actionUiState.value = JeuActionUiState.Loading
            jeuRepository.deleteJeu(idJeu, libelleJeu)
                .onSuccess {
                    _actionUiState.value = JeuActionUiState.Success("Jeu supprimé avec succès")
                }
                .onFailure { e ->
                    _actionUiState.value = JeuActionUiState.Error(e.message ?: "Erreur lors de la suppression")
                }
        }
    }

    /**
     * Réinitialise l'état des actions
     */
    fun resetActionState() {
        _actionUiState.value = JeuActionUiState.Idle
    }
}
