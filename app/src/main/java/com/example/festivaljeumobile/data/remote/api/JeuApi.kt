package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.JeuDto
import retrofit2.http.GET

interface JeuApi {
    @GET("/api/jeux/getAllJeux")
    suspend fun getAllJeux(): List<JeuDto>
}
