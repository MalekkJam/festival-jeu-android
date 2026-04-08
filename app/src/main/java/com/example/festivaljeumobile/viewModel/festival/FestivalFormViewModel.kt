package com.example.festivaljeumobile.viewModel.festival

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import com.example.festivaljeumobile.domain.repository.FestivalRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ZoneTarifaireFormUiState(
    val id: Int? = null,
    val nom: String = "",
    val nbTables: String = "",
    val prixDuM2: String = "",
)

data class FestivalFormUiState(
    val festivalId: Long? = null,
    val nom: String = "",
    val dateDebut: String = "",
    val dateFin: String = "",
    val fallbackNbTables: Int = 0,
    val zoneCount: String = "",
    val zonesTarifaires: List<ZoneTarifaireFormUiState> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isEditMode: Boolean
        get() = festivalId != null

    val totalNbTables: Int
        get() = if (zonesTarifaires.isEmpty()) fallbackNbTables else zonesTarifaires.sumOf { it.nbTables.toIntOrNull() ?: 0 }
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
        if (festival == null) {
            _uiState.value = FestivalFormUiState()
            return
        }

        val zones = festival.zoneTarifaires.map { zone ->
            ZoneTarifaireFormUiState(
                id = zone.id,
                nom = zone.nom,
                nbTables = zone.nbTables.toString(),
                prixDuM2 = zone.prixDuM2.toString()
            )
        }
        _uiState.value = FestivalFormUiState(
            festivalId = festival.id,
            nom = festival.nom,
            dateDebut = festival.date_debut.toInputDate(),
            dateFin = festival.date_fin.toInputDate(),
            fallbackNbTables = festival.nbTables,
            zoneCount = zones.size.toString(),
            zonesTarifaires = zones
        )

        if (festival.id > 0L && zones.isEmpty()) {
            loadZones(festival.id)
        }
    }

    private fun loadZones(festivalId: Long) {
        viewModelScope.launch {
            festivalRepository.getZonesForFestival(festivalId).fold(
                onSuccess = { zones ->
                    _uiState.update {
                        it.copy(
                            zoneCount = zones.size.toString(),
                            zonesTarifaires = zones.map { zone ->
                                ZoneTarifaireFormUiState(
                                    id = zone.id,
                                    nom = zone.nom,
                                    nbTables = zone.nbTables.toString(),
                                    prixDuM2 = zone.prixDuM2.toString()
                                )
                            },
                            error = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            error = it.error ?: throwable.message
                        )
                    }
                }
            )
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

    fun onZoneCountChange(value: String) {
        val sanitized = value.filter { it.isDigit() }
        val targetCount = sanitized.toIntOrNull() ?: 0
        _uiState.update { currentState ->
            currentState.copy(
                zoneCount = sanitized,
                zonesTarifaires = currentState.zonesTarifaires.resizeTo(targetCount),
                error = null
            )
        }
    }

    fun onZoneNomChange(index: Int, value: String) {
        updateZone(index) { it.copy(nom = value) }
    }

    fun onZoneNbTablesChange(index: Int, value: String) {
        val sanitized = value.filter { char -> char.isDigit() }
        updateZone(index) { it.copy(nbTables = sanitized) }
    }

    fun onZonePrixDuM2Change(index: Int, value: String) {
        val sanitized = value.filter { char -> char.isDigit() }
        updateZone(index) { it.copy(prixDuM2 = sanitized) }
    }

    fun submit() {
        val state = _uiState.value
        val nom = state.nom.trim()
        val dateDebut = state.dateDebut.trim()
        val dateFin = state.dateFin.trim()
        val zoneCount = state.zoneCount.toIntOrNull() ?: 0

        when {
            nom.isBlank() || dateDebut.isBlank() || dateFin.isBlank() || state.zoneCount.isBlank() -> {
                _uiState.update { it.copy(error = "Veuillez remplir tous les champs du festival.") }
                return
            }

            zoneCount <= 0 -> {
                _uiState.update { it.copy(error = "Ajoutez au moins une zone tarifaire.") }
                return
            }

            !dateDebut.isValidDateInput() || !dateFin.isValidDateInput() -> {
                _uiState.update { it.copy(error = "Les dates doivent etre au format YYYY-MM-DD.") }
                return
            }
        }

        val zonesTarifaires = state.zonesTarifaires.mapIndexed { index, zone ->
            val nbTables = zone.nbTables.toIntOrNull()
            val prixDuM2 = zone.prixDuM2.toIntOrNull()

            when {
                zone.nom.trim().isBlank() -> {
                    _uiState.update {
                        it.copy(error = "Le nom de la zone ${index + 1} est obligatoire.")
                    }
                    return
                }

                nbTables == null || nbTables <= 0 -> {
                    _uiState.update {
                        it.copy(error = "Le nombre de tables de la zone ${index + 1} doit etre superieur a 0.")
                    }
                    return
                }

                prixDuM2 == null || prixDuM2 < 0 -> {
                    _uiState.update {
                        it.copy(error = "Le prix du m2 de la zone ${index + 1} doit etre valide.")
                    }
                    return
                }
            }

            ZoneTarifaire(
                id = zone.id,
                nom = zone.nom.trim(),
                nbTables = nbTables,
                prixDuM2 = prixDuM2
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val festival = Festival(
                id = state.festivalId ?: 0L,
                nom = nom,
                date_debut = dateDebut.toApiDate(),
                date_fin = dateFin.toApiDate(),
                nbTables = zonesTarifaires.sumOf { it.nbTables },
                zoneTarifaires = zonesTarifaires
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
                            error = throwable.message ?: "Impossible d'enregistrer le festival."
                        )
                    }
                }
            )
        }
    }

    private fun updateZone(
        index: Int,
        transform: (ZoneTarifaireFormUiState) -> ZoneTarifaireFormUiState,
    ) {
        _uiState.update { currentState ->
            if (index !in currentState.zonesTarifaires.indices) {
                currentState
            } else {
                currentState.copy(
                    zonesTarifaires = currentState.zonesTarifaires.mapIndexed { currentIndex, zone ->
                        if (currentIndex == index) transform(zone) else zone
                    },
                    error = null
                )
            }
        }
    }
}

private fun List<ZoneTarifaireFormUiState>.resizeTo(targetCount: Int): List<ZoneTarifaireFormUiState> =
    when {
        targetCount <= 0 -> emptyList()
        size == targetCount -> this
        size > targetCount -> take(targetCount)
        else -> this + List(targetCount - size) { ZoneTarifaireFormUiState() }
    }

private fun String.isValidDateInput(): Boolean {
    val regex = Regex("""\d{4}-\d{2}-\d{2}""")
    return matches(regex)
}

private fun String.toApiDate(): String = "${this}T00:00:00.000Z"
private fun String.toInputDate(): String = substringBefore("T")
