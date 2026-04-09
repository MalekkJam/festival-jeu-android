package com.example.festivaljeumobile.viewModel.reservant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.model.ReservantType
import com.example.festivaljeumobile.domain.repository.ReservantRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État UI du formulaire de réservant.
 * Représente tous les champs et états du formulaire.
 */
data class ReservantFormUiState(
    val reservantId: Int? = null,
    val nom: String = "",
    val prenom: String = "",
    val type: ReservantType = ReservantType.Association, // Type par défaut
    val email: String = "",
    val telephone: String = "",
    val entreprise: String = "",
    val adresse: String = "",
    val codePostal: String = "",
    val ville: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    /**
     * Est-ce en mode édition (vs création) ?
     */
    val isEditMode: Boolean
        get() = reservantId != null
}

/**
 * Événements que le formulaire peut émettre.
 */
sealed class ReservantFormEvent {
    object Saved : ReservantFormEvent()
}

/**
 * ViewModel pour la gestion du formulaire Réservant (créer/éditer).
 * Expose un StateFlow<UiState> pour les changements UI réactifs.
 * Utilise un Channel pour les événements uniques (sauvegarde complète).
 */
class ReservantFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reservantRepository: ReservantRepository
        get() = (getApplication<Application>() as FestivalApp).reservantRepository

    private val _uiState = MutableStateFlow(ReservantFormUiState())
    val uiState: StateFlow<ReservantFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<ReservantFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Initialise le formulaire avec un réservant existant (édition).
     * Si null, le formulaire est en mode création.
     */
    fun setInitialReservant(reservant: Reservant?) {
        if (reservant == null) {
            _uiState.value = ReservantFormUiState()
            return
        }

        _uiState.value = ReservantFormUiState(
            reservantId = reservant.id,
            nom = reservant.nom,
            prenom = reservant.prenom,
            type = reservant.type,
            email = reservant.email.orEmpty(),
            telephone = reservant.telephone.orEmpty(),
            entreprise = reservant.entreprise.orEmpty(),
            adresse = reservant.adresse.orEmpty(),
            codePostal = reservant.codePostal.orEmpty(),
            ville = reservant.ville.orEmpty(),
        )
    }

    /**
     * Callbacks pour changements de champs individuels.
     */
    fun onNomChange(value: String) {
        _uiState.update { it.copy(nom = value, error = null) }
    }

    fun onTypeChange(value: ReservantType) {
        _uiState.update { it.copy(type = value, error = null) }
    }

    fun onPrenomChange(value: String) {
        _uiState.update { it.copy(prenom = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onTelephoneChange(value: String) {
        _uiState.update { it.copy(telephone = value, error = null) }
    }

    fun onEntrepriseChange(value: String) {
        _uiState.update { it.copy(entreprise = value, error = null) }
    }

    fun onAdresseChange(value: String) {
        _uiState.update { it.copy(adresse = value, error = null) }
    }

    fun onCodePostalChange(value: String) {
        _uiState.update { it.copy(codePostal = value, error = null) }
    }

    fun onVilleChange(value: String) {
        _uiState.update { it.copy(ville = value, error = null) }
    }

    /**
     * Valide le formulaire et enregistre le réservant (création ou mise à jour).
     */
    fun saveReservant() {
        val state = _uiState.value

        // Validation
        when {
            state.nom.isBlank() -> {
                _uiState.update { it.copy(error = "Le nom est obligatoire.") }
                return
            }
            state.prenom.isBlank() -> {
                _uiState.update { it.copy(error = "Le prenom est obligatoire.") }
                return
            }
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val reservant = Reservant(
                id = state.reservantId ?: 0,
                nom = state.nom.trim(),
                type = state.type,
                prenom = state.prenom.trim(),
                email = state.email.takeIf { it.isNotBlank() },
                telephone = state.telephone.takeIf { it.isNotBlank() },
                entreprise = state.entreprise.takeIf { it.isNotBlank() },
                adresse = state.adresse.takeIf { it.isNotBlank() },
                codePostal = state.codePostal.takeIf { it.isNotBlank() },
                ville = state.ville.takeIf { it.isNotBlank() },
            )

            val result = if (state.isEditMode) {
                reservantRepository.update(reservant)
            } else {
                reservantRepository.create(reservant)
            }

            result.fold(
                onSuccess = {
                    // Réinitialise complètement le formulaire (pattern Festival)
                    // L'ID serveur est sauvegardé en DB par le repository
                    // La liste se rafraîchira via le Flow avec les bons ID's
                    _uiState.value = ReservantFormUiState()
                    _events.send(ReservantFormEvent.Saved)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = throwable.message ?: "Erreur lors de la sauvegarde."
                        )
                    }
                }
            )
        }
    }
}
