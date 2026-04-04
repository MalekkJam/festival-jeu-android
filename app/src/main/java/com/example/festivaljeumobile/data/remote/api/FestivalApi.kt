package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.DeleteFestivalRequestDto
import com.example.festivaljeumobile.data.remote.dto.FestivalDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT

interface FestivalApi {
    @GET("/api/festival/getAllFestivals")
    suspend fun getAllFestivals(): List<FestivalDto>

    @POST("/api/festival/addFestival")
    suspend fun addFestival(@Body festival: FestivalDto): FestivalDto

    @PUT("/api/festival/updateFestival")
    suspend fun updateFestival(@Body festival: FestivalDto): FestivalDto

    @HTTP(method = "DELETE", path = "/api/festival/deleteFestival", hasBody = true)
    suspend fun deleteFestival(@Body request: DeleteFestivalRequestDto)
}
