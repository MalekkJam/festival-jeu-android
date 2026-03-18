package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.User

interface AuthRepository {
    suspend fun login(login: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun whoAmI(): Result<User>
}