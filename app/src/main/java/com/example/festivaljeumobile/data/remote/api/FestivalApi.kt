package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.DeleteFestivalRequestDto
import com.example.festivaljeumobile.data.remote.dto.FestivalDto
import com.example.festivaljeumobile.data.remote.dto.ZoneTarifaireDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT

interface FestivalApi {
    @GET("/api/festival/getAllFestivals")
    suspend fun getAllFestivals(): List<FestivalDto>

    @GET("/api/zoneTarifaire/getAllZones/{festivalId}")
    suspend fun getAllZones(@Path("festivalId") festivalId: Long): List<ZoneTarifaireDto>

    @POST("/api/festival/addFestival")
    suspend fun addFestival(@Body festival: FestivalDto): FestivalDto

    @POST("/api/zoneTarifaire/addZone/{festivalId}")
    suspend fun addZone(
        @Path("festivalId") festivalId: Long,
        @Body zone: ZoneTarifaireDto
    ): ZoneTarifaireDto

    @PUT("/api/festival/updateFestival")
    suspend fun updateFestival(@Body festival: FestivalDto): FestivalDto

    @PUT("/api/zoneTarifaire/updateZone/{id}")
    suspend fun updateZone(
        @Path("id") zoneId: Int,
        @Body zone: ZoneTarifaireDto
    )

    @HTTP(method = "DELETE", path = "/api/festival/deleteFestival", hasBody = true)
    suspend fun deleteFestival(@Body request: DeleteFestivalRequestDto)
}
