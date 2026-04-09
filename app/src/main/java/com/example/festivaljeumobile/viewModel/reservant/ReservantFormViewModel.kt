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

data class ReservantFormUiState(
    val reservantId: Int? = null,
    val nom: String = "",
    val type: ReservantType = ReservantType.Association,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isEditMode: Boolean
        get() = reservantId != null
}

sealed class ReservantFormEvent {
    object Saved : ReservantFormEvent()
}

class ReservantFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reservantRepository: ReservantRepository
        get() = (getApplication<Application>() as FestivalApp).reservantRepository

    private val _uiState = MutableStateFlow(ReservantFormUiState())
    val uiState: StateFlow<ReservantFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<ReservantFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setInitialReservant(reservant: Reservant?) {
        _uiState.value = if (reservant == null) {
            ReservantFormUiState()
        } else {
            ReservantFormUiState(
                reservantId = reservant.id,
                nom = reservant.nom,
                type = reservant.type,
            )
        }
    }

    fun onNomChange(value: String) {
        _uiState.update { it.copy(nom = value, error = null) }
    }

    fun onTypeChange(value: ReservantType) {
        _uiState.update { it.copy(type = value, error = null) }
    }

    fun saveReservant() {
        val state = _uiState.value

        if (state.nom.isBlank()) {
            _uiState.update { it.copy(error = "Le nom est obligatoire.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val reservant = Reservant(
                id = state.reservantId ?: 0,
                nom = state.nom.trim(),
                type = state.type,
            )

            val result = if (state.isEditMode) {
                reservantRepository.update(reservant)
            } else {
                reservantRepository.create(reservant)
            }

            result.fold(
                onSuccess = {
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
