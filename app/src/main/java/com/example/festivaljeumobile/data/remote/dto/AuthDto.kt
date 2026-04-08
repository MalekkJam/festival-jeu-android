package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto (
    val login : String,
    val password : String
)

@Serializable
data class LoginResponseDto (
    val user: UserResponseDto
)

@Serializable
data class WhoAmIResponseWrapper (
    val user: WhoAmIResponseDto
)

@Serializable
data class WhoAmIResponseDto (
    val id : Long,
    val role : UserRole,
    val login : String? = null,
    val iat : Long? = null,
    val exp : Long? = null
)

@Serializable
data class UserResponseDto (
    val id : Long? = null,
    val login : String,
    val role : UserRole,
    val prenom : String? = null,
    val nom : String? = null
)

