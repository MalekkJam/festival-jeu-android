package com.example.festivaljeumobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.festivaljeumobile.data.local.entity.ReservantEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour les opérations Room sur les réservants.
 * Gère les requêtes de lecture/écriture dans la base locale.
 */
@Dao
interface ReservantDao {
    /**
     * Observe tous les réservants en temps réel (Flow reactif).
     * S'émet automatiquement lors de changements en base.
     */
    @Query("SELECT * FROM reservants")
    fun observeAll(): Flow<List<ReservantEntity>>

    /**
     * Récupère un réservant spécifique par ID (requête synchrone).
     */
    @Query("SELECT * FROM reservants WHERE id = :id")
    suspend fun getById(id: Int): ReservantEntity?

    /**
     * Insert ou update (upsert) une liste d'entités.
     * Utilisé pour synchroniser depuis l'API distante.
     */
    @Upsert
    suspend fun upsertAll(entities: List<ReservantEntity>)

    /**
     * Insert ou update une entité unique.
     */
    @Upsert
    suspend fun upsert(entity: ReservantEntity)

    /**
     * Supprime un réservant de la base locale.
     */
    @Delete
    suspend fun delete(entity: ReservantEntity)

    /**
     * Supprime tous les réservants (optionnel, pour reset complet).
     */
    @Query("DELETE FROM reservants")
    suspend fun deleteAll()
}
