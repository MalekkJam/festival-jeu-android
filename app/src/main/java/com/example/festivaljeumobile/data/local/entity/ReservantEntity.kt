package com.example.festivaljeumobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.model.ReservantType

/**
 * Entité Room pour cache local des réservants
 * Permet une persistance hors ligne avec synchronisation distante
 */
@Entity(tableName = "reservants")
data class ReservantEntity(
    @PrimaryKey val id: Int,
    val nom: String,
    val type: String, // Stored as String, converted to enum on read
    val prenom: String = "",
    val email: String? = null,
    val telephone: String? = null,
    val entreprise: String? = null,
    val adresse: String? = null,
    val codePostal: String? = null,
    val ville: String? = null,
) {
    /**
     * Convertit l'entité Room vers le modèle métier Domain
     */
    fun toDomain(): Reservant =
        Reservant(
            id = id,
            nom = nom,
            type = ReservantType.valueOf(type),
            prenom = prenom,
            email = email,
            telephone = telephone,
            entreprise = entreprise,
            adresse = adresse,
            codePostal = codePostal,
            ville = ville,
        )
}
