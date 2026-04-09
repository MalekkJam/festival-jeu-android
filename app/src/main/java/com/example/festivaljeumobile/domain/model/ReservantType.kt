package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

/**
 * Énumération des types de réservants (entités qui réservent des tables)
 * Doit correspondre à l'énumération TypeReservant du backend Prisma
 */
@Serializable
enum class ReservantType {
    Editeur,
    Prestataire,
    Association,
    Boutique,
    Animation
}
