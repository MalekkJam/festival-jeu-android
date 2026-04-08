package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY id DESC")
    fun observeAll(): Flow<List<ReservationEntity>>

    @Upsert
    suspend fun upsertAll(reservations: List<ReservationEntity>)

    @Transaction
    suspend fun replaceAll(reservations: List<ReservationEntity>) {
        deleteAll()
        upsertAll(reservations)
    }

    @Query("DELETE FROM reservations")
    suspend fun deleteAll()

    @Query("DELETE FROM reservations WHERE id = :reservationId")
    suspend fun deleteById(reservationId: Long)
}
