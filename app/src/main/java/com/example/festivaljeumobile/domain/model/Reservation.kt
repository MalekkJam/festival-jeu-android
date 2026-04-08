package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation (
    val id: Long? = null,
    val reservantId : Int,
    val festivalId : Int,
    val festivalName: String,
    val reservantName: String,
    val etatDeSuivi : String,
    val dateDeContact : String? = null,
    val remise : String? = null,
    val montantRemise : Int? = null,
    val facturee : Boolean,
    val payee : Boolean,
    val jeux : List<ReservationJeu>,
)
