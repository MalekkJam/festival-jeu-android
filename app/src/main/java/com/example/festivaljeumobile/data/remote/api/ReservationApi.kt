package com.example.festivaljeumobile.data.remote.api

import com.example.festivaljeumobile.data.remote.dto.CreateReservationRequestDto
import com.example.festivaljeumobile.data.remote.dto.ReservationDto
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface ReservationApi {
    @GET("/api/reservation/getAllReservations")
    suspend fun getAllReservations(): List<ReservationDto>

    @POST("/api/reservation/addReservation")
    suspend fun addReservation(@Body reservation: CreateReservationRequestDto): ReservationDto

    @PUT("/api/reservation/updateReservation")
    suspend fun updateReservation(@Body reservation: CreateReservationRequestDto): ReservationDto

    @DELETE("/api/reservation/deleteReservation/{reservationId}")
    suspend fun deleteReservation(@Path("reservationId") reservationId: Long)
}
