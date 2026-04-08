package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.domain.model.ReservantOption
import kotlinx.serialization.Serializable

@Serializable
data class ReservantDto(
    val id: Int,
    val nom: String,
)

fun ReservantDto.toReservantOption(): ReservantOption =
    ReservantOption(
        id = id,
        nom = nom
    )
