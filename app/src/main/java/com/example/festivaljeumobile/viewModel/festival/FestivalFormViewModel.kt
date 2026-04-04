package com.example.festivaljeumobile.viewModel.festival

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.repository.FestivalRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FestivalFormUiState(
    val festivalId: Long? = null,
    val nom: String = "",
    val dateDebut: String = "",
    val dateFin: String = "",
    val nbTables: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isEditMode: Boolean
        get() = festivalId != null
}

sealed class FestivalFormEvent {
    object Saved : FestivalFormEvent()
}

class FestivalFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val festivalRepository: FestivalRepository
        get() = (getApplication<Application>() as FestivalApp).festivalRepository

    private val _uiState = MutableStateFlow(FestivalFormUiState())
    val uiState: StateFlow<FestivalFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<FestivalFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setInitialFestival(festival: Festival?) {
        _uiState.update {
            if (festival == null) {
                FestivalFormUiState()
            } else {
                FestivalFormUiState(
                    festivalId = festival.id,
                    nom = festival.nom,
                    dateDebut = festival.date_debut.toInputDate(),
                    dateFin = festival.date_fin.toInputDate(),
                    nbTables = festival.nbTables.toString()
                )
            }
        }
    }

    fun onNomChange(value: String) {
        _uiState.update { it.copy(nom = value, error = null) }
    }

    fun onDateDebutChange(value: String) {
        _uiState.update { it.copy(dateDebut = value, error = null) }
    }

    fun onDateFinChange(value: String) {
        _uiState.update { it.copy(dateFin = value, error = null) }
    }

    fun onNbTablesChange(value: String) {
        val sanitized = value.filter { it.isDigit() }
        _uiState.update { it.copy(nbTables = sanitized, error = null) }
    }

    fun submit() {
        val state = _uiState.value
        val nom = state.nom.trim()
        val dateDebut = state.dateDebut.trim()
        val dateFin = state.dateFin.trim()
        val nbTables = state.nbTables.toIntOrNull()

        when {
            nom.isBlank() || dateDebut.isBlank() || dateFin.isBlank() || state.nbTables.isBlank() -> {
                _uiState.update { it.copy(error = "Veuillez remplir tous les champs.") }
                return
            }

            nbTables == null || nbTables <= 0 -> {
                _uiState.update { it.copy(error = "Le nombre de tables doit etre superieur a 0.") }
                return
            }

            !dateDebut.isValidDateInput() || !dateFin.isValidDateInput() -> {
                _uiState.update { it.copy(error = "Les dates doivent etre au format YYYY-MM-DD.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val festival = Festival(
                id = state.festivalId ?: 0L,
                nom = nom,
                date_debut = dateDebut.toApiDate(),
                date_fin = dateFin.toApiDate(),
                nbTables = nbTables
            )

            val result = if (state.isEditMode) {
                festivalRepository.update(festival)
            } else {
                festivalRepository.create(festival)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = FestivalFormUiState()
                    _events.send(FestivalFormEvent.Saved)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = throwable.message ?: "Impossible de creer le festival."
                        )
                    }
                }
            )
        }
    }
}

private fun String.isValidDateInput(): Boolean {
    val regex = Regex("""\d{4}-\d{2}-\d{2}""")
    return matches(regex)
}

private fun String.toApiDate(): String = "${this}T00:00:00.000Z"
private fun String.toInputDate(): String = substringBefore("T")
