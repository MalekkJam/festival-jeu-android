package com.example.festivaldujeu.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.example.festivaldujeu.data.remote.dto.EditeurDto
import com.example.festivaldujeu.data.remote.dto.TypeJeuDto

/**
 * Interface Retrofit pour les endpoints "Éditeurs"
 * Utilisée par JeuFormScreen pour les dropdowns editeur
 */
interface EditeurApi {
    @GET("/api/editeurs/getAllEditeurs")
    suspend fun getAllEditeurs(): List<EditeurDto>

    @GET("/api/editeurs/{id}")
    suspend fun getEditeurById(@Query id: Int): EditeurDto?
}

/**
 * Interface Retrofit pour les endpoints "TypeJeu"
 * Utilisée par JeuFormScreen pour les dropdowns typeJeu
 */
interface TypeJeuApi {
    @GET("/api/type-jeu/getAllTypeJeu")
    suspend fun getAllTypeJeu(): List<TypeJeuDto>
}
