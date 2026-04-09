package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.DeleteReservantRequestDto
import com.example.festivaljeumobile.data.remote.dto.ReservantDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Service Retrofit pour les opérations CRUD sur les réservants.
 * API backend : Node.js + Express 5
 * Pattern Festival : POST add, PUT update, HTTP DELETE with body
 */
interface ReservantApi {
    @GET("/api/reservant/getAllReservants")
    suspend fun getAllReservants(): List<ReservantDto>

    @GET("/api/reservant/getReservant/{id}")
    suspend fun getReservantById(@Path("id") id: Int): ReservantDto

    @POST("/api/reservant/addReservant")
    suspend fun addReservant(@Body reservant: ReservantDto): ReservantDto

    @PUT("/api/reservant/updateReservant")
    suspend fun updateReservant(@Body reservant: ReservantDto): ReservantDto

    @HTTP(method = "DELETE", path = "/api/reservant/deleteReservant", hasBody = true)
    suspend fun deleteReservant(@Body request: DeleteReservantRequestDto): Unit
}