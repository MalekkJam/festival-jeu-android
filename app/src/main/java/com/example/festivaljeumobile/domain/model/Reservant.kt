package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

/**
 * Entité métier Reservant (pure business logic)
 * Représente une personne/entité qui réserve des tables à un festival
 * Conforme DIP - Domain Layer indépendante des sources de données
 */
@Serializable
data class Reservant(
    val id: Int,
    val nom: String,
    val type: ReservantType,
)
