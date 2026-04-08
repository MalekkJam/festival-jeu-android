package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.CreateUserRequestDto
import com.example.festivaljeumobile.data.remote.dto.DeleteUserRequestDto
import com.example.festivaljeumobile.data.remote.dto.UpdateUserRequestDto
import com.example.festivaljeumobile.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Interface Retrofit pour les endpoints API utilisateurs
 * Rôle requis : Admin uniquement
 * Base URL : http://<host>:4000/api/user
 */
interface UserApi {

    /**
     * GET /api/user/getAllUsers
     * Retourne tous les utilisateurs non-Admin
     */
    @GET("/api/user/getAllUsers")
    suspend fun getAllUsers(): List<UserDto>

    /**
     * POST /api/user/addUser
     * Crée un nouvel utilisateur
     */
    @POST("/api/user/addUser")
    suspend fun addUser(@Body request: CreateUserRequestDto): UserDto

    /**
     * PUT /api/user/updateUser
     * Met à jour un utilisateur existant
     */
    @PUT("/api/user/updateUser")
    suspend fun updateUser(@Body request: UpdateUserRequestDto): UserDto

    /**
     * DELETE /api/user/deleteUser
     * Supprime un utilisateur
     */
    @HTTP(method = "DELETE", path = "/api/user/deleteUser", hasBody = true)
    suspend fun deleteUser(@Body request: DeleteUserRequestDto)
}
