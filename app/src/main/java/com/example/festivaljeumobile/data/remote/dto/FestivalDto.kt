package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import com.example.festivaljeumobile.domain.model.Festival
import kotlinx.serialization.Serializable

@Serializable
data class FestivalDto(
    val id: Long,
    val nom: String,
    val image: String? = null,
    val date_debut: String,
    val date_fin: String,
    val nbTables: Int,
)

fun FestivalDto.toEntity(): FestivalEntity =
    FestivalEntity(
        id = id,
        nom = nom,
        date_debut = date_debut,
        date_fin = date_fin,
        nbTables = nbTables
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
        nbTables = nbTables
    )
