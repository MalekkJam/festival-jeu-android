package com.example.festivaldujeu.domain.model

/**
 * Entité métier Jeu (pure, sans annotations Room/Retrofit)
 * Conforme à la règle DIP - Domain Layer indépendante des sources de données
 */
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
