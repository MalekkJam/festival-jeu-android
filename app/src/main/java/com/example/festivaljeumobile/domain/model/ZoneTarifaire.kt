package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ZoneTarifaire(
    val id: Int? = null,
    val nom: String,
    val nbTables: Int,
    val prixDuM2: Int,
)
