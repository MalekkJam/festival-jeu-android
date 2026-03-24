package com.example.festivaljeumobile.data.mapper

import com.example.festivaljeumobile.data.remote.dto.JeuDto
import com.example.festivaljeumobile.domain.model.Jeu

/**
 * Mappeurs pour convertir entre les DTOs Retrofit et le modèle Domain
 * Version simplifiée sans Room (pas d'Entity)
 */

// DTO (Retrofit) → Domain
fun JeuDto.toDomain(): Jeu = Jeu(
    idJeu = idJeu,
    libelleJeu = libelleJeu,
    auteurJeu = auteurJeu,
    nbMinJoueurJeu = nbMinJoueurJeu,
    nbMaxJoueurJeu = nbMaxJoueurJeu,
    noticeJeu = noticeJeu,
    idEditeur = idEditeur,
    idTypeJeu = idTypeJeu,
    agemini = agemini,
    prototype = prototype,
    duree = duree,
    theme = theme,
    description = description,
    imageJeu = imageJeu,
    videoRegle = videoRegle
)

// Domain → DTO (Retrofit)
fun Jeu.toDto(): JeuDto = JeuDto(
    idJeu = idJeu,
    libelleJeu = libelleJeu,
    auteurJeu = auteurJeu,
    nbMinJoueurJeu = nbMinJoueurJeu,
    nbMaxJoueurJeu = nbMaxJoueurJeu,
    noticeJeu = noticeJeu,
    idEditeur = idEditeur,
    idTypeJeu = idTypeJeu,
    agemini = agemini,
    prototype = prototype,
    duree = duree,
    theme = theme,
    description = description,
    imageJeu = imageJeu,
    videoRegle = videoRegle
)

// List extensions
fun List<JeuDto>.toDomain(): List<Jeu> = map { it.toDomain() }
fun List<Jeu>.toDto(): List<JeuDto> = map { it.toDto() }
