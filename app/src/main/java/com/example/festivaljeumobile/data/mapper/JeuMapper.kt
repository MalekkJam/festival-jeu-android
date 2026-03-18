package com.example.festivaldujeu.data.mapper

import com.example.festivaldujeu.data.local.entity.JeuEntity
import com.example.festivaldujeu.data.remote.dto.JeuDto
import com.example.festivaldujeu.domain.model.Jeu

/**
 * Mappeurs pour convertir entre les couches (Domain ← → Data)
 * Évite de mélanger les dépendances
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

// Entity (Room) → Domain
fun JeuEntity.toDomain(): Jeu = Jeu(
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

// Domain → Entity (Room)
fun Jeu.toEntity(): JeuEntity = JeuEntity(
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

// List<Entity> → List<Domain>
fun List<JeuEntity>.toDomain(): List<Jeu> = map { it.toDomain() }

// List<DTO> → List<Domain>
fun List<JeuDto>.toDomain(): List<Jeu> = map { it.toDomain() }
