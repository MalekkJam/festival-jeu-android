package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.remote.api.AuthApi
import com.example.festivaljeumobile.data.remote.dto.LoginRequestDto
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            Log.d("LOGIN", ("WE HERE IN THE LOGIN"));
            val response = authApi.login(LoginRequestDto(login, password))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Identifiants incorrects."))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau."))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = authApi.logout()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erreur lors de la déconnexion."))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau."))
        }
    }

    override suspend fun whoAmI(): Result<User> {
        return try {
            val response = authApi.whoAmI()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(User(id = body.id, role = body.role, login ="", password = null,prenom = null,nom = null))
            } else Result.failure(Exception("Non authentifié."))
        } catch (e: Exception) {
            Result.failure(Exception("Erreur réseau."))
        }
    }
}