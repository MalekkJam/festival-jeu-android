package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservationJeu (
    val id: Long? = null,
    val jeuId: Int,
    val zoneTarifaireId: Int,
    val typeTable: String,
    val nbTables: Int,
    val place: Boolean,
    val jeu: Jeu? = null,
)
