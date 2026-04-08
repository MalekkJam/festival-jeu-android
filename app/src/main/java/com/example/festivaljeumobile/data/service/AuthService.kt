package com.example.festivaljeumobile.data.service

import android.util.Log
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.data.remote.api.AuthApi
import com.example.festivaljeumobile.data.remote.dto.LoginRequestDto
import com.example.festivaljeumobile.data.repository.AuthRepositoryImpl
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class AuthService(
    private val authRepository: AuthRepository = AuthRepositoryImpl(
        RetrofitInstance.retrofit.create(AuthApi::class.java)
    )
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Computed property: user is logged in if currentUser is not null
    val isLoggedIn: Flow<Boolean> = _currentUser.map { user -> user != null }

    suspend fun login(login: String, password: String): Result<Unit> {
        return try {
            authRepository.login(login, password).fold(
                onSuccess = {
                    Log.d("AuthService", "Login success, fetching user info...")
                    // Fetch current user after successful login
                    authRepository.whoAmI().fold(
                        onSuccess = { user ->
                            _currentUser.value = user
                            Result.success(Unit)
                        },
                        onFailure = { error ->
                            Log.e("AuthService", "Failed to fetch user after login: ${error.message}")
                            Result.failure(error)
                        }
                    )
                },
                onFailure = { error ->
                    Log.e("AuthService", "Login failed: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e("AuthService", "Login exception: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            authRepository.logout().fold(
                onSuccess = {
                    _currentUser.value = null
                    Log.d("AuthService", "Logout success")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    _currentUser.value = null
                    Log.e("AuthService", "Logout error: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _currentUser.value = null
            Log.e("AuthService", "Logout exception: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Verify if user is still logged in by calling whoAmI()
     * Used at app startup to restore session from cookies
     */
    suspend fun verifySession(): Result<Unit> {
        return try {
            authRepository.whoAmI().fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    Log.d("AuthService", "Session verified for user: ${user.login}")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    _currentUser.value = null
                    Log.d("AuthService", "Session verification failed: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _currentUser.value = null
            Log.e("AuthService", "Session verification exception: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private var instance: AuthService? = null

        fun getInstance(): AuthService {
            if (instance == null) {
                instance = AuthService()
            }
            return instance!!
        }
    }
}
