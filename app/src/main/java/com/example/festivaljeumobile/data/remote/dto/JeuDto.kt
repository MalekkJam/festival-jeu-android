package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.domain.model.Jeu
import kotlinx.serialization.Serializable

@Serializable
data class JeuDto(
    val idJeu: Int,
    val libelleJeu: String,
)

fun JeuDto.toDomain(): Jeu =
    Jeu(
        idJeu = idJeu,
        libelleJeu = libelleJeu
    )
