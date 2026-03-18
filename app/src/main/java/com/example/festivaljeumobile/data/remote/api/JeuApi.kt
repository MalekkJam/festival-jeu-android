package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.JeuDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Interface Retrofit pour les endpoints API jeux
 * Base URL : http://<host>:4000
*/
interface JeuApi {
    /**
     * GET /api/jeux/getAllJeux
     * Rôles requis : Organisateur, SuperOrganisateur, Admin
     */
    @GET("/api/jeux/getAllJeux")
    suspend fun getAllJeux(): List<JeuDto>

    /**
     * POST /api/jeux/addJeu
     * Rôles requis : SuperOrganisateur, Admin
     * Body : JeuDto complet (sans idJeu)
     */
    @POST("/api/jeux/addJeu")
    suspend fun addJeu(@Body jeu: JeuDto): JeuDto

    /**
     * POST /api/jeux/updateJeu
     * Rôles requis : SuperOrganisateur, Admin
     * Body : JeuDto avec idJeu
     */
    @POST("/api/jeux/updateJeu")
    suspend fun updateJeu(@Body jeu: JeuDto): JeuDto

    /**
     * POST /api/jeux/deleteJeu
     * Rôles requis : SuperOrganisateur, Admin
     * Body : { id: Int, nom: String }
     */
    @POST("/api/jeux/deleteJeu")
    suspend fun deleteJeu(@Body request: DeleteJeuRequest): Unit

    data class DeleteJeuRequest(
        val id: Int,
        val nom: String
    )
}
