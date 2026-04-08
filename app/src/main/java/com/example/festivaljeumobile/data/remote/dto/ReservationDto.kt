package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.data.local.entity.ReservationEntity
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ReservationJeu
import kotlinx.serialization.Serializable

@Serializable
data class ReservationDto(
    val id: Long,
    val reservantId: Int,
    val festivalId: Int,
    val festivalName: String,
    val reservantName: String,
    val etatDeSuivi: String,
    val dateDeContact: String? = null,
    val remise: String? = null,
    val montantRemise: Int? = null,
    val facturee: Boolean,
    val payee: Boolean,
    val jeux: List<ReservationJeuDto> = emptyList(),
)

@Serializable
data class ReservationJeuDto(
    val id: Long? = null,
    val jeuId: Int,
    val zoneTarifaireId: Int,
    val typeTable: String,
    val nbTables: Int,
    val place: Boolean,
)

fun ReservationDto.toEntity(): ReservationEntity =
    ReservationEntity(
        id = id,
        reservantId = reservantId,
        festivalId = festivalId,
        festivalName = festivalName,
        reservantName = reservantName,
        etatDeSuivi = etatDeSuivi,
        dateDeContact = dateDeContact,
        remise = remise,
        montantRemise = montantRemise,
        facturee = facturee,
        payee = payee,
        jeux = jeux.map { it.toDomain() }
    )

private fun ReservationJeuDto.toDomain(): ReservationJeu =
    ReservationJeu(
        id = id,
        jeuId = jeuId,
        zoneTarifaireId = zoneTarifaireId,
        typeTable = typeTable,
        nbTables = nbTables,
        place = place
    )

fun Reservation.toDto(): ReservationDto =
    ReservationDto(
        id = id ?: 0L,
        reservantId = reservantId,
        festivalId = festivalId,
        festivalName = festivalName,
        reservantName = reservantName,
        etatDeSuivi = etatDeSuivi,
        dateDeContact = dateDeContact,
        remise = remise,
        montantRemise = montantRemise,
        facturee = facturee,
        payee = payee,
        jeux = jeux.map { it.toDto() }
    )

private fun ReservationJeu.toDto(): ReservationJeuDto =
    ReservationJeuDto(
        id = id,
        jeuId = jeuId,
        zoneTarifaireId = zoneTarifaireId,
        typeTable = typeTable,
        nbTables = nbTables,
        place = place
    )

@Serializable
data class CreateReservationRequestDto(
    val id: Long? = null,
    val festivalId: Int,
    val reservantId: Int,
    val etatDeSuivi: String? = null,
    val dateDeContact: String? = null,
    val remise: String? = null,
    val montantRemise: Int? = null,
    val facturee: Boolean = false,
    val payee: Boolean = false,
    val jeux: List<CreateReservationJeuDto> = emptyList(),
)

@Serializable
data class CreateReservationJeuDto(
    val jeuId: Int,
    val zoneTarifaireId: Int,
    val zonePlanId: Int? = null,
    val typeTable: String,
    val nbTables: Int,
    val place: Boolean = false,
    )

fun Reservation.toSaveRequestDto(): CreateReservationRequestDto =
    CreateReservationRequestDto(
        id = id,
        festivalId = festivalId,
        reservantId = reservantId,
        etatDeSuivi = etatDeSuivi.ifBlank { null },
        dateDeContact = dateDeContact,
        remise = remise?.ifBlank { null },
        montantRemise = montantRemise,
        facturee = facturee,
        payee = payee,
        jeux = jeux.map { jeu ->
            CreateReservationJeuDto(
                jeuId = jeu.jeuId,
                zoneTarifaireId = jeu.zoneTarifaireId,
                zonePlanId = null,
                typeTable = jeu.typeTable,
                nbTables = jeu.nbTables,
                place = jeu.place
            )
        }
    )
