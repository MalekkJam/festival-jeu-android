package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.local.preferences.CookieDataStore
import com.example.festivaljeumobile.data.remote.api.AuthApi
import com.example.festivaljeumobile.data.remote.dto.LoginRequestDto
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val cookieDataStore: CookieDataStore? = null,
) : AuthRepository {

    override suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            Log.d("LOGIN", "Attempting login...")
            val response = authApi.login(LoginRequestDto(login, password))
            if (response.isSuccessful) {
                cookieDataStore?.writeUserRole(response.body()?.user?.role)
                Log.d("LOGIN", "Login successful")
                Result.success(Unit)
            } else {
                Log.w("LOGIN", "Login failed with status: ${response.code()}")
                Result.failure(Exception("Identifiants incorrects."))
            }
        } catch (e: Exception) {
            Log.e("LOGIN", "Login error: ${e.message}", e)
            Result.failure(Exception("Erreur reseau."))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = authApi.logout()
            cookieDataStore?.clearCookies()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erreur lors de la deconnexion."))
        } catch (e: Exception) {
            cookieDataStore?.clearCookies()
            Result.failure(Exception("Erreur reseau."))
        }
    }

    override suspend fun whoAmI(): Result<User> {
        return try {
            val response = authApi.whoAmI()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                cookieDataStore?.writeUserRole(body.user.role)
                Result.success(
                    User(
                        id = body.user.id,
                        login = body.user.login ?: "Unknown",
                        role = body.user.role
                    )
                )
            } else {
                Log.e("WHOAMI", "whoAmI failed with status: ${response.code()}")
                Result.failure(Exception("Non authentifie."))
            }
        } catch (e: Exception) {
            Log.e("WHOAMI", "whoAmI error: ${e.message}")
            Result.failure(Exception("Erreur reseau."))
        }
    }
}
