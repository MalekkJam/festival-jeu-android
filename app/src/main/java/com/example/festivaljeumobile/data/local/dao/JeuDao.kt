package com.example.festivaldujeu.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.festivaldujeu.data.local.entity.JeuEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour la persistance des jeux
 * Émet automatiquement les changements via Flow
 */
@Dao
interface JeuDao {
    /**
     * Récupère tous les jeux avec observer automatique
     */
    @Query("SELECT * FROM jeus ORDER BY libelleJeu ASC")
    fun getAllJeux(): Flow<List<JeuEntity>>

    /**
     * Récupère un jeu par ID
     */
    @Query("SELECT * FROM jeus WHERE idJeu = :idJeu")
    suspend fun getJeuById(idJeu: Int): JeuEntity?

    /**
     * Insère ou remplace un jeu (merge offline-first)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJeu(jeu: JeuEntity)

    /**
     * Insère ou remplace plusieurs jeux atomiquement
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllJeux(jeux: List<JeuEntity>)

    /**
     * Met à jour un jeu
     */
    @Update
    suspend fun updateJeu(jeu: JeuEntity)

    /**
     * Supprime un jeu
     */
    @Delete
    suspend fun deleteJeu(jeu: JeuEntity)

    /**
     * Supprime un jeu par ID
     */
    @Query("DELETE FROM jeus WHERE idJeu = :idJeu")
    suspend fun deleteJeuById(idJeu: Int)

    /**
     * Compte le nombre total de jeux
     */
    @Query("SELECT COUNT(*) FROM jeus")
    suspend fun count(): Int

    /**
     * Nettoie toute la table (sync offline)
     */
    @Query("DELETE FROM jeus")
    suspend fun clearAll()
}
