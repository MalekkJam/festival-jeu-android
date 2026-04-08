package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.JeuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JeuDao {
    @Query("SELECT * FROM jeux ORDER BY libelleJeu ASC")
    fun observeAll(): Flow<List<JeuEntity>>

    @Upsert
    suspend fun upsertAll(jeux: List<JeuEntity>)

    @Transaction
    suspend fun replaceAll(jeux: List<JeuEntity>) {
        deleteAll()
        upsertAll(jeux)
    }

    @Upsert
    suspend fun upsert(jeu: JeuEntity)

    @Query("SELECT * FROM jeux WHERE idJeu = :idJeu LIMIT 1")
    suspend fun getById(idJeu: Int): JeuEntity?

    @Query("DELETE FROM jeux WHERE idJeu = :idJeu")
    suspend fun deleteById(idJeu: Int)

    @Query("DELETE FROM jeux")
    suspend fun deleteAll()
}
