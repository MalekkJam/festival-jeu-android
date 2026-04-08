package com.example.festivaljeumobile.viewModel.reservation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ReservationJeu
import com.example.festivaljeumobile.domain.model.ReservantOption
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import com.example.festivaljeumobile.domain.repository.ReservationRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val DEFAULT_TYPE_TABLE = "petite"
private const val SUBMIT_TIMEOUT_MS = 20_000L

data class ReservationJeuFormUiState(
    val jeuId: Int? = null,
    val zoneTarifaireId: Int? = null,
    val nbTables: String = "",
    val place: Boolean = false,
)

data class ReservationFormUiState(
    val reservationId: Long? = null,
    val initialFestivalName: String = "",
    val initialReservantName: String = "",
    val reservants: List<ReservantOption> = emptyList(),
    val festivals: List<Festival> = emptyList(),
    val zonesTarifaires: List<ZoneTarifaire> = emptyList(),
    val jeuxDisponibles: List<Jeu> = emptyList(),
    val selectedReservantId: Int? = null,
    val selectedFestivalId: Long? = null,
    val jeux: List<ReservationJeuFormUiState> = listOf(ReservationJeuFormUiState()),
    val etatDeSuivi: String = "",
    val dateDeContact: String = "",
    val remise: String = "",
    val montantRemise: String = "",
    val facturee: Boolean = false,
    val payee: Boolean = false,
    val isLoadingOptions: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isEditMode: Boolean
        get() = reservationId != null
}

sealed class ReservationFormEvent {
    object Saved : ReservationFormEvent()
}

class ReservationFormViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reservationRepository: ReservationRepository
        get() = (getApplication<Application>() as FestivalApp).reservationRepository

    private val _uiState = MutableStateFlow(ReservationFormUiState())
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<ReservationFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadOptions()
    }

    fun setInitialReservation(reservation: Reservation?) {
        if (reservation == null) {
            _uiState.update {
                it.copy(
                reservationId = null,
                initialFestivalName = "",
                initialReservantName = "",
                selectedReservantId = null,
                selectedFestivalId = null,
                    zonesTarifaires = emptyList(),
                    jeux = listOf(ReservationJeuFormUiState()),
                    etatDeSuivi = "",
                    dateDeContact = "",
                    remise = "",
                    montantRemise = "",
                    facturee = false,
                    payee = false,
                    error = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                reservationId = reservation.id,
                initialFestivalName = reservation.festivalName,
                initialReservantName = reservation.reservantName,
                selectedReservantId = reservation.reservantId,
                selectedFestivalId = reservation.festivalId.toLong(),
                jeux = if (reservation.jeux.isEmpty()) {
                    listOf(ReservationJeuFormUiState())
                } else {
                    reservation.jeux.map { jeu ->
                    ReservationJeuFormUiState(
                        jeuId = jeu.jeuId,
                        zoneTarifaireId = jeu.zoneTarifaireId,
                        nbTables = jeu.nbTables.toString(),
                        place = jeu.place
                    )
                    }
                },
                etatDeSuivi = reservation.etatDeSuivi,
                dateDeContact = reservation.dateDeContact?.substringBefore("T").orEmpty(),
                remise = reservation.remise.orEmpty(),
                montantRemise = reservation.montantRemise?.toString().orEmpty(),
                facturee = reservation.facturee,
                payee = reservation.payee,
                error = null
            )
        }
        loadZones(reservation.festivalId.toLong())
    }

    fun loadOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingOptions = true, error = null) }

            val reservantsResult = reservationRepository.getReservants()
            val festivalsResult = reservationRepository.getFestivals()
            val jeuxResult = reservationRepository.getJeux()

            val firstError = listOf(
                reservantsResult.exceptionOrNull(),
                festivalsResult.exceptionOrNull(),
                jeuxResult.exceptionOrNull()
            ).firstOrNull()

            _uiState.update {
                it.copy(
                    reservants = reservantsResult.getOrDefault(emptyList()),
                    festivals = festivalsResult.getOrDefault(emptyList()),
                    jeuxDisponibles = jeuxResult.getOrDefault(emptyList()),
                    isLoadingOptions = false,
                    error = firstError?.message
                )
            }
        }
    }

    fun onReservantSelected(reservantId: Int) {
        _uiState.update { it.copy(selectedReservantId = reservantId, error = null) }
    }

    fun onFestivalSelected(festivalId: Long) {
        _uiState.update {
            it.copy(
                selectedFestivalId = festivalId,
                zonesTarifaires = emptyList(),
                jeux = it.jeux.map { jeu -> jeu.copy(zoneTarifaireId = null) },
                error = null
            )
        }
        loadZones(festivalId)
    }

    private fun loadZones(festivalId: Long) {
        viewModelScope.launch {
            reservationRepository.getZonesForFestival(festivalId).fold(
                onSuccess = { zones ->
                    _uiState.update { it.copy(zonesTarifaires = zones, error = null) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            zonesTarifaires = emptyList(),
                            error = throwable.message ?: "Impossible de charger les zones tarifaires."
                        )
                    }
                }
            )
        }
    }

    fun onEtatDeSuiviChange(value: String) {
        _uiState.update { it.copy(etatDeSuivi = value, error = null) }
    }

    fun onDateDeContactChange(value: String) {
        _uiState.update { it.copy(dateDeContact = value, error = null) }
    }

    fun onRemiseChange(value: String) {
        _uiState.update { current ->
            current.copy(
                remise = value,
                montantRemise = if (value.isBlank()) "" else current.montantRemise,
                error = null
            )
        }
    }

    fun onMontantRemiseChange(value: String) {
        val sanitized = value.filter { it.isDigit() }
        _uiState.update { it.copy(montantRemise = sanitized, error = null) }
    }

    fun onFactureeChange(value: Boolean) {
        _uiState.update { it.copy(facturee = value, error = null) }
    }

    fun onPayeeChange(value: Boolean) {
        _uiState.update { it.copy(payee = value, error = null) }
    }

    fun addJeuLine() {
        _uiState.update {
            it.copy(
                jeux = it.jeux + ReservationJeuFormUiState(),
                error = null
            )
        }
    }

    fun removeJeuLine(index: Int) {
        _uiState.update { current ->
            val nextJeux = current.jeux.toMutableList().also {
                if (index in it.indices) {
                    it.removeAt(index)
                }
                if (it.isEmpty()) {
                    it.add(ReservationJeuFormUiState())
                }
            }
            current.copy(jeux = nextJeux, error = null)
        }
    }

    fun onJeuSelected(index: Int, jeuId: Int) {
        updateJeu(index) { it.copy(jeuId = jeuId) }
    }

    fun onZoneSelected(index: Int, zoneId: Int) {
        updateJeu(index) { it.copy(zoneTarifaireId = zoneId) }
    }

    fun onJeuNbTablesChange(index: Int, value: String) {
        val sanitized = value.filter { it.isDigit() }
        updateJeu(index) { it.copy(nbTables = sanitized) }
    }

    fun onJeuPlaceChange(index: Int, value: Boolean) {
        updateJeu(index) { it.copy(place = value) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) {
            return
        }

        val reservant = state.reservants.firstOrNull { it.id == state.selectedReservantId }
        val festival = state.festivals.firstOrNull { it.id == state.selectedFestivalId }

        when {
            reservant == null -> {
                _uiState.update { it.copy(error = "Selectionnez un reservant.") }
                return
            }

            festival == null -> {
                _uiState.update { it.copy(error = "Selectionnez un festival.") }
                return
            }

            state.jeux.isEmpty() -> {
                _uiState.update { it.copy(error = "Ajoutez au moins un jeu.") }
                return
            }
        }

        val reservationJeux = state.jeux.mapIndexed { index, jeuState ->
            val jeuId = jeuState.jeuId
            val zoneId = jeuState.zoneTarifaireId
            val nbTables = jeuState.nbTables.toIntOrNull()

            when {
                jeuId == null -> {
                    _uiState.update { it.copy(error = "Selectionnez le jeu de la ligne ${index + 1}.") }
                    return
                }

                zoneId == null -> {
                    _uiState.update { it.copy(error = "Selectionnez la zone tarifaire de la ligne ${index + 1}.") }
                    return
                }

                nbTables == null || nbTables <= 0 -> {
                    _uiState.update { it.copy(error = "Le nombre de tables de la ligne ${index + 1} doit etre superieur a 0.") }
                    return
                }
            }

            ReservationJeu(
                jeuId = jeuId,
                zoneTarifaireId = zoneId,
                typeTable = DEFAULT_TYPE_TABLE,
                nbTables = nbTables,
                place = jeuState.place
            )
        }

        if (state.dateDeContact.isNotBlank() && !state.dateDeContact.isValidDateInput()) {
            _uiState.update { it.copy(error = "La date de contact doit etre au format YYYY-MM-DD.") }
            return
        }

        val reservation = Reservation(
            id = state.reservationId,
            reservantId = reservant.id,
            festivalId = festival.id.toInt(),
            festivalName = festival.nom,
            reservantName = reservant.nom,
            etatDeSuivi = state.etatDeSuivi,
            dateDeContact = state.dateDeContact.trim().ifBlank { null }?.toApiDate(),
            remise = state.remise.ifBlank { null },
            montantRemise = state.montantRemise.toIntOrNull(),
            facturee = state.facturee,
            payee = state.payee,
            jeux = reservationJeux
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val result = withTimeoutOrNull(SUBMIT_TIMEOUT_MS) {
                if (state.isEditMode) {
                    reservationRepository.update(reservation)
                } else {
                    reservationRepository.create(reservation)
                }
            } ?: Result.failure(Exception("La requete prend trop de temps. Reessayez."))

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _events.send(ReservationFormEvent.Saved)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = throwable.message ?: "Impossible de creer la reservation."
                        )
                    }
                }
            )
        }
    }

    private fun updateJeu(
        index: Int,
        transform: (ReservationJeuFormUiState) -> ReservationJeuFormUiState,
    ) {
        _uiState.update { current ->
            if (index !in current.jeux.indices) {
                current
            } else {
                current.copy(
                    jeux = current.jeux.mapIndexed { currentIndex, jeu ->
                        if (currentIndex == index) transform(jeu) else jeu
                    },
                    error = null
                )
            }
        }
    }
}

private fun String.isValidDateInput(): Boolean {
    val regex = Regex("""\d{4}-\d{2}-\d{2}""")
    return matches(regex)
}

private fun String.toApiDate(): String = "${this}T00:00:00.000Z"
