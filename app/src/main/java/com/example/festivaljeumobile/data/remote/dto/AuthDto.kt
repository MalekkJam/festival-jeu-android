package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto (
    val login : String,
    val password : String
)

@Serializable
data class WhoAmIResponseDto (
    val id : Long,
    val role : UserRole
)

