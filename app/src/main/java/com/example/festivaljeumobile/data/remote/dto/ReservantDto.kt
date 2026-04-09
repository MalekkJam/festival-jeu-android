package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.data.local.entity.ReservantEntity
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.model.ReservantOption
import com.example.festivaljeumobile.domain.model.ReservantType
import kotlinx.serialization.Serializable

/**
 * DTO pour sérialisation JSON Retrofit.
 * Représente un réservant tel que reçu du backend.
 * Respecte la structure backend Express + Prisma.
 * 
 * Note: id=0 en POST (création) - le backend ignore et génère l'vrai id
 * Le backend retourne toujours l'id dans la réponse
 */
@Serializable
data class ReservantDto(
    val id: Int,
    val nom: String,
    val type: ReservantType,
    val prenom: String = "",
    val email: String? = null,
    val telephone: String? = null,
    val entreprise: String? = null,
    val adresse: String? = null,
    val codePostal: String? = null,
    val ville: String? = null,
)

/**
 * DTO pour demande de suppression d'un réservant.
 * Envoyé en body d'une requête HTTP DELETE.
 */
@Serializable
data class DeleteReservantRequestDto(
    val id: Int
)

/**
 * Mapper : ReservantDto → ReservantEntity
 */
fun ReservantDto.toEntity(): ReservantEntity =
    ReservantEntity(
        id = id,
        nom = nom,
        type = type.name,
        prenom = prenom,
        email = email,
        telephone = telephone,
        entreprise = entreprise,
        adresse = adresse,
        codePostal = codePostal,
        ville = ville
    )

/**
 * Mapper : List<ReservantDto> → List<ReservantEntity>
 */
fun List<ReservantDto>.toEntity(): List<ReservantEntity> =
    map { it.toEntity() }

/**
 * Mapper : ReservantDto → Reservant (domain)
 */
fun ReservantDto.toDomain(): Reservant =
    Reservant(
        id = id,
        nom = nom,
        type = type,
        prenom = prenom,
        email = email,
        telephone = telephone,
        entreprise = entreprise,
        adresse = adresse,
        codePostal = codePostal,
        ville = ville
    )

/**
 * Mapper : ReservantEntity → Reservant (domain)
 */
fun ReservantEntity.toDomain(): Reservant =
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
        ville = ville
    )

/**
 * Mapper : Reservant (domain) → DeleteReservantRequestDto (API request)
 * Utilisé lors de la suppression d'un réservant.
 */
fun Reservant.toDeleteRequestDto(): DeleteReservantRequestDto =
    DeleteReservantRequestDto(id = id)

/**
 * Mapper : Reservant (domain) → ReservantDto (API)
 */
fun Reservant.toDto(): ReservantDto =
    ReservantDto(
        id = id,
        nom = nom,
        type = type,
        prenom = prenom,
        email = email,
        telephone = telephone,
        entreprise = entreprise,
        adresse = adresse,
        codePostal = codePostal,
        ville = ville
    )

/**
 * Mapper : Reservant (domain) → ReservantEntity
 */
fun Reservant.toEntity(): ReservantEntity =
    ReservantEntity(
        id = id,
        nom = nom,
        type = type.name,
        prenom = prenom,
        email = email,
        telephone = telephone,
        entreprise = entreprise,
        adresse = adresse,
        codePostal = codePostal,
        ville = ville
    )

/**
 * Convertit un DTO au format simplifié ReservantOption.
 * Utilisé pour les listes de sélection (ex: lors d'une réservation).
 */
fun ReservantDto.toReservantOption(): ReservantOption =
    ReservantOption(
        id = id,
        nom = nom
    )
