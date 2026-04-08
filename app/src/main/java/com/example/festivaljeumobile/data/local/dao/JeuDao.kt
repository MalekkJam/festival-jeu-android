package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.JeuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JeuDao {
    @Query("SELECT * FROM jeux ORDER BY libelleJeu ASC")
    fun observeAll(): Flow<List<JeuEntity>>

    @Query("SELECT * FROM jeux WHERE idJeu = :jeuId")
    suspend fun getById(jeuId: Int): JeuEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM jeux LIMIT 1)")
    suspend fun hasJeux(): Boolean

    @Query("DELETE FROM jeux WHERE idJeu = :jeuId")
    suspend fun deleteById(jeuId: Int)

    @Upsert
    suspend fun upsertAll(jeux: List<JeuEntity>)

    @Query("DELETE FROM jeux")
    suspend fun deleteAll()
}
