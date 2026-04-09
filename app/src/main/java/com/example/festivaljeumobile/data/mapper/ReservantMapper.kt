package com.example.festivaljeumobile.data.mapper

import com.example.festivaljeumobile.data.local.entity.ReservantEntity
import com.example.festivaljeumobile.data.remote.api.ReservantApi
import com.example.festivaljeumobile.data.remote.dto.ReservantDto
import com.example.festivaljeumobile.domain.model.Reservant

/**
 * Mappeurs pour convertir entre les DTOs Retrofit, les Entities Room,
 * et le modèle Domain.
 * Respecte le pattern de séparation des couches (DIP).
 */

// ============= DTO (Retrofit) ↔ Domain =============

/**
 * Convertit un DTO reçu du backend vers le modèle métier.
 */
fun ReservantDto.toDomain(): Reservant = Reservant(
    id = id,
    nom = nom,
    type = type,
    prenom = prenom,
    email = email,
    telephone = telephone,
    entreprise = entreprise,
    adresse = adresse,
    codePostal = codePostal,
    ville = ville,
)

/**
 * Convertit un modèle métier vers un DTO pour envoi au backend.
 * Envoie id même si 0 (le backend ignora pour créations).
 * Pattern : identique à Jeu (idJeu toujours envoyé, 0 pour création)
 */
fun Reservant.toDto(): ReservantDto = ReservantDto(
    id = id, // Envoie même si 0 pour création
    nom = nom,
    type = type,
    prenom = prenom,
    email = email,
    telephone = telephone,
    entreprise = entreprise,
    adresse = adresse,
    codePostal = codePostal,
    ville = ville,
)

// ============= DTO ↔ Entity (Room) =============

/**
 * Convertit un DTO vers une entité Room pour cache local.
 */
fun ReservantDto.toEntity(): ReservantEntity = ReservantEntity(
    id = id,
    nom = nom,
    type = type.name,
    prenom = prenom,
    email = email,
    telephone = telephone,
    entreprise = entreprise,
    adresse = adresse,
    codePostal = codePostal,
    ville = ville,
)

// ============= Entity ↔ Domain (direct, via toDomain() d'Entity) =============
// Voir ReservantEntity.toDomain()

/**
 * Convertit un modèle métier vers une entité Room pour stockage local.
 */
fun Reservant.toEntity(): ReservantEntity = ReservantEntity(
    id = id,
    nom = nom,
    type = type.name,
    prenom = prenom,
    email = email,
    telephone = telephone,
    entreprise = entreprise,
    adresse = adresse,
    codePostal = codePostal,
    ville = ville,
)

// ============= List extensions =============

/**
 * Convertit une liste de DTOs vers une liste de modèles Domain.
 */
fun List<ReservantDto>.toDomain(): List<Reservant> = map { it.toDomain() }

/**
 * Convertit une liste de Entities vers une liste de modèles Domain.
 */
fun List<ReservantEntity>.asDomain(): List<Reservant> = map { it.toDomain() }

/**
 * Convertit une liste de DTOs vers une liste d'Entities.
 */
fun List<ReservantDto>.toEntity(): List<ReservantEntity> = map { it.toEntity() }
