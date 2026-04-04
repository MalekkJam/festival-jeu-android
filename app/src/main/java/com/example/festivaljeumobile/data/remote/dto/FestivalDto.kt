package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.data.local.entity.FestivalEntity
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
