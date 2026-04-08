package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Jeu(
    val idJeu: Int,
    val libelleJeu: String,
    val auteurJeu: String? = null,
    val nbMinJoueurJeu: Int? = null,
    val nbMaxJoueurJeu: Int? = null,
    val noticeJeu: String? = null,
    val idEditeur: Int? = null,
    val idTypeJeu: Int? = null,
    val agemini: Int? = null,
    val prototype: Boolean? = null,
    val duree: Int? = null,
    val theme: String? = null,
    val description: String? = null,
    val imageJeu: String? = null,
    val videoRegle: String? = null
)

