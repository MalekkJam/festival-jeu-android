package com.example.festivaldujeu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO pour sérialisation JSON Retrofit
 * Respecte la structure backend Express
 */
@Serializable
data class JeuDto(
    @SerialName("idJeu")
    val idJeu: Int,
    @SerialName("libelleJeu")
    val libelleJeu: String,
    @SerialName("auteurJeu")
    val auteurJeu: String? = null,
    @SerialName("nbMinJoueurJeu")
    val nbMinJoueurJeu: Int? = null,
    @SerialName("nbMaxJoueurJeu")
    val nbMaxJoueurJeu: Int? = null,
    @SerialName("noticeJeu")
    val noticeJeu: String? = null,
    @SerialName("idEditeur")
    val idEditeur: Int? = null,
    @SerialName("idTypeJeu")
    val idTypeJeu: Int? = null,
    @SerialName("agemini")
    val agemini: Int? = null,
    @SerialName("prototype")
    val prototype: Boolean? = null,
    @SerialName("duree")
    val duree: Int? = null,
    @SerialName("theme")
    val theme: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("imageJeu")
    val imageJeu: String? = null,
    @SerialName("videoRegle")
    val videoRegle: String? = null
)
