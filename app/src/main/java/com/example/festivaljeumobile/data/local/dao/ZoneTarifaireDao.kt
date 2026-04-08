package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.ZoneTarifaireEntity

@Dao
interface ZoneTarifaireDao {
    @Query("SELECT * FROM zone_tarifaires WHERE festivalId = :festivalId ORDER BY id ASC")
    suspend fun getByFestivalId(festivalId: Long): List<ZoneTarifaireEntity>

    @Query("DELETE FROM zone_tarifaires WHERE festivalId = :festivalId")
    suspend fun deleteByFestivalId(festivalId: Long)

    @Query("DELETE FROM zone_tarifaires WHERE id = :zoneId")
    suspend fun deleteById(zoneId: Int)

    @Upsert
    suspend fun upsertAll(zones: List<ZoneTarifaireEntity>)
}
