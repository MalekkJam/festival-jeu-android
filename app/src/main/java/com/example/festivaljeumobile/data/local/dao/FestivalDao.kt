package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FestivalDao {
    @Query("SELECT * FROM festivals ORDER BY date_debut ASC")
    fun observeAll(): Flow<List<FestivalEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM festivals LIMIT 1)")
    suspend fun hasFestivals(): Boolean

    @Upsert
    suspend fun upsertAll(festivals: List<FestivalEntity>)
}
