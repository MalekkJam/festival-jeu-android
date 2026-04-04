package com.example.festivaljeumobile.domain.model

data class Festival(
    val id: Long,
    val nom: String,
    val date_debut: String,
    val date_fin: String,
    val nbTables: Int,
)
