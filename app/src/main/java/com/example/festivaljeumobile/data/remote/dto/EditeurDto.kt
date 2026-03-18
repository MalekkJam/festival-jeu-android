package com.example.festivaldujeu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO Éditeur (utilisé dans les dropdowns du formulaire jeu)
 */
@Serializable
data class EditeurDto(
    @SerialName("idEditeur")
    val idEditeur: Int,
    @SerialName("libelleEditeur")
    val libelleEditeur: String
)

/**
 * DTO TypeJeu (utilisé dans les dropdowns du formulaire jeu)
 */
@Serializable
data class TypeJeuDto(
    @SerialName("idTypeJeu")
    val idTypeJeu: Int,
    @SerialName("libelleTypeJeu")
    val libelleTypeJeu: String
)
