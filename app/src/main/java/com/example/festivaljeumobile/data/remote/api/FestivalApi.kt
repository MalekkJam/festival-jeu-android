package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.FestivalDto
import retrofit2.http.GET

interface FestivalApi {
    @GET("/api/festival/getAllFestivals")
    suspend fun getAllFestivals(): List<FestivalDto>
}
