package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ReservantOption
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun getAll(): Flow<List<Reservation>>
    suspend fun refresh(): Result<Unit>
    suspend fun getReservants(): Result<List<ReservantOption>>
    suspend fun getFestivals(): Result<List<Festival>>
    suspend fun getZonesForFestival(festivalId: Long): Result<List<ZoneTarifaire>>
    suspend fun getJeux(): Result<List<Jeu>>
    suspend fun create(reservation: Reservation): Result<Unit>
    suspend fun update(reservation: Reservation): Result<Unit>
    suspend fun delete(reservationId: Long): Result<Unit>
}
