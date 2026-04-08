package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.ReservantDto
import retrofit2.http.GET

interface ReservantApi {
    @GET("/api/reservant/getAllReservants")
    suspend fun getAllReservants(): List<ReservantDto>
}
