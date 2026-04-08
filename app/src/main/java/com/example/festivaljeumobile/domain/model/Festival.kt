package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Festival(
    val id: Long,
    val nom: String,
    val date_debut: String,
    val date_fin: String,
    val nbTables: Int,
    val zoneTarifaires: List<ZoneTarifaire> = emptyList(),
)
