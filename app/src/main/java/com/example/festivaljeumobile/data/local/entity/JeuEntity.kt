package com.example.festivaljeumobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room pour la persistance locale (cache offline-first)
 */
@Entity(tableName = "jeux")
data class JeuEntity(
    @PrimaryKey
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
