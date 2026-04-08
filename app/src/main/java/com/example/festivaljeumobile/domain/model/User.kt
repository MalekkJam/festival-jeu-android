package com.example.festivaljeumobile.domain.model

data class User(
    val id: Int? = null,
    val login: String,
    val role: UserRole,
    val password: String? = null,
    val prenom: String? = null,
    val nom: String? = null
)