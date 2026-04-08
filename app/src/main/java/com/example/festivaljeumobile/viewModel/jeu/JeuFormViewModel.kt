package com.example.festivaljeumobile.viewModel.jeu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel pour le détail d'un jeu et la gestion des actions (add/update)
 * Accède au repository via FestivalApp comme JeuListViewModel
 */
class JeuFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val jeuRepository: JeuRepository
        get() = (getApplication<Application>() as FestivalApp).jeuRepository

    private val _detailUiState = MutableStateFlow<JeuDetailUiState>(JeuDetailUiState.Loading)
    val detailUiState: StateFlow<JeuDetailUiState> = _detailUiState.asStateFlow()

    private val _actionUiState = MutableStateFlow<JeuActionUiState>(JeuActionUiState.Idle)
    val actionUiState: StateFlow<JeuActionUiState> = _actionUiState.asStateFlow()

    /**
     * Charge un jeu par ID
     */
    fun loadJeu(idJeu: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _detailUiState.update { JeuDetailUiState.Loading }
            jeuRepository.getJeuById(idJeu)
                .onSuccess { jeu ->
                    if (jeu != null) {
                        _detailUiState.update { JeuDetailUiState.Success(jeu) }
                    } else {
                        _detailUiState.update { JeuDetailUiState.NotFound }
                    }
                }
                .onFailure { e ->
                    _detailUiState.update { JeuDetailUiState.Error(e.message ?: "Erreur inconnue") }
                }
        }
    }

    /**
     * Crée un nouveau jeu
     */
    fun addJeu(jeu: Jeu) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionUiState.update { JeuActionUiState.Loading }
            jeuRepository.addJeu(jeu)
                .onSuccess {
                    _actionUiState.update { JeuActionUiState.Success("Jeu créé avec succès") }
                }
                .onFailure { e ->
                    _actionUiState.update { JeuActionUiState.Error(e.message ?: "Erreur lors de la création") }
                }
        }
    }

    /**
     * Met à jour un jeu existant
     */
    fun updateJeu(jeu: Jeu) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionUiState.update { JeuActionUiState.Loading }
            jeuRepository.updateJeu(jeu)
                .onSuccess {
                    _actionUiState.update { JeuActionUiState.Success("Jeu mis à jour avec succès") }
                }
                .onFailure { e ->
                    _actionUiState.update { JeuActionUiState.Error(e.message ?: "Erreur lors de la mise à jour") }
                }
        }
    }

    /**
     * Supprime un jeu
     */
    fun deleteJeu(idJeu: Int, libelleJeu: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionUiState.update { JeuActionUiState.Loading }
            jeuRepository.deleteJeu(idJeu, libelleJeu)
                .onSuccess {
                    _actionUiState.update { JeuActionUiState.Success("Jeu supprimé avec succès") }
                }
                .onFailure { e ->
                    _actionUiState.update { JeuActionUiState.Error(e.message ?: "Erreur lors de la suppression") }
                }
        }
    }

    /**
     * Réinitialise l'état des actions
     */
    fun resetActionState() {
        _actionUiState.update { JeuActionUiState.Idle }
    }
}

sealed class JeuDetailUiState {
    data object Loading : JeuDetailUiState()
    data class Success(val jeu: Jeu) : JeuDetailUiState()
    data class Error(val message: String) : JeuDetailUiState()
    data object NotFound : JeuDetailUiState()
}

sealed class JeuActionUiState {
    data object Idle : JeuActionUiState()
    data object Loading : JeuActionUiState()
    data class Success(val message: String) : JeuActionUiState()
    data class Error(val message: String) : JeuActionUiState()
}
