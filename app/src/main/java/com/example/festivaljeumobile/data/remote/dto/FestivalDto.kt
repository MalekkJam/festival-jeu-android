package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import kotlinx.serialization.Serializable

@Serializable
data class FestivalDto(
    val id: Long? = null,
    val nom: String,
    val image: String? = null,
    val date_debut: String,
    val date_fin: String,
    val nbTables: Int? = null,
    val zones: List<ZoneTarifaireDto> = emptyList(),
)

@Serializable
data class ZoneTarifaireDto(
    val id: Int? = null,
    val nom: String,
    val nbTables: Int,
    val prixDuM2: Int,
)

fun FestivalDto.toEntity(): FestivalEntity =
    FestivalEntity(
        id = id ?: 0L,
        nom = nom,
        date_debut = date_debut,
        date_fin = date_fin,
        nbTables = nbTables ?: zones.sumOf { it.nbTables },
        zoneTarifaires = zones.map { it.toDomain() }
    )

@Serializable
data class DeleteFestivalRequestDto(
    val id: Long,
    val nom: String,
)

fun Festival.toDeleteRequestDto(): DeleteFestivalRequestDto =
    DeleteFestivalRequestDto(
        id = id,
        nom = nom
    )

fun Festival.toDto(): FestivalDto =
    FestivalDto(
        id = id,
        nom = nom,
        image = null,
        date_debut = date_debut,
        date_fin = date_fin,
        nbTables = nbTables,
        zones = zoneTarifaires.map { it.toDto() }
    )

fun ZoneTarifaireDto.toDomain(): ZoneTarifaire =
    ZoneTarifaire(
        id = id,
        nom = nom,
        nbTables = nbTables,
        prixDuM2 = prixDuM2
    )

fun ZoneTarifaire.toDto(): ZoneTarifaireDto =
    ZoneTarifaireDto(
        id = id,
        nom = nom,
        nbTables = nbTables,
        prixDuM2 = prixDuM2
    )
