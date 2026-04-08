package com.example.festivaljeumobile.data.remote.dto

import com.example.festivaljeumobile.domain.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long? = null,
    val login: String,
    val prenom: String? = null,
    val nom: String? = null,
    val role: UserRole,
    val password: String? = null,
)

@Serializable
data class CreateUserRequestDto(
    val login: String,
    val password: String,
    val prenom: String? = null,
    val nom: String? = null,
    val role: UserRole,
)

@Serializable
data class UpdateUserRequestDto(
    val id: Long,
    val login: String,
    val prenom: String? = null,
    val nom: String? = null,
    val role: UserRole,
    val password: String? = null,
)

@Serializable
data class DeleteUserRequestDto(
    val id: Long,
)