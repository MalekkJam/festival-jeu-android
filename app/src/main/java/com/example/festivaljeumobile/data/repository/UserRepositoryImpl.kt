package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.remote.api.UserApi
import com.example.festivaljeumobile.data.remote.dto.CreateUserRequestDto
import com.example.festivaljeumobile.data.remote.dto.DeleteUserRequestDto
import com.example.festivaljeumobile.data.remote.dto.UpdateUserRequestDto
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole
import com.example.festivaljeumobile.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val userApi: UserApi,
) : UserRepository {

    override suspend fun getAllUsers(): Result<List<User>> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = userApi.getAllUsers()
                val users = dtos.map { dto ->
                    User(
                        id = dto.id,
                        login = dto.login,
                        role = dto.role,
                        prenom = dto.prenom,
                        nom = dto.nom,
                    )
                }
                Result.success(users)
            } catch (throwable: Throwable) {
                Log.e("UserRepository", "getAllUsers error: ${throwable.message}")
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de charger les utilisateurs hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de récupérer les utilisateurs.")
                    }
                )
            }
        }

    override suspend fun addUser(
        login: String,
        password: String,
        prenom: String?,
        nom: String?,
        role: UserRole,
    ): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val dto = userApi.addUser(
                    CreateUserRequestDto(
                        login = login,
                        password = password,
                        prenom = prenom,
                        nom = nom,
                        role = role,
                    )
                )
                Result.success(
                    User(
                        id = dto.id,
                        login = dto.login,
                        role = dto.role,
                        prenom = dto.prenom,
                        nom = dto.nom,
                    )
                )
            } catch (throwable: Throwable) {
                Log.e("UserRepository", "addUser error: ${throwable.message}")
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Création impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de créer l'utilisateur.")
                    }
                )
            }
        }

    override suspend fun updateUser(
        id: Long,
        login: String,
        prenom: String?,
        nom: String?,
        role: UserRole,
        password: String?,
    ): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val dto = userApi.updateUser(
                    UpdateUserRequestDto(
                        id = id,
                        login = login,
                        prenom = prenom,
                        nom = nom,
                        role = role,
                        password = password?.ifBlank { null },
                    )
                )
                Result.success(
                    User(
                        id = dto.id,
                        login = dto.login,
                        role = dto.role,
                        prenom = dto.prenom,
                        nom = dto.nom,
                    )
                )
            } catch (throwable: Throwable) {
                Log.e("UserRepository", "updateUser error: ${throwable.message}")
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Modification impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de modifier l'utilisateur.")
                    }
                )
            }
        }

    override suspend fun deleteUser(id: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                userApi.deleteUser(DeleteUserRequestDto(id = id))
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e("UserRepository", "deleteUser error: ${throwable.message}")
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Suppression impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de supprimer l'utilisateur.")
                    }
                )
            }
        }
}
