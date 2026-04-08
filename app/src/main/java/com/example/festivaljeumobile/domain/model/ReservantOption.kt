package com.example.festivaljeumobile.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReservantOption(
    val id: Int,
    val nom: String,
)
