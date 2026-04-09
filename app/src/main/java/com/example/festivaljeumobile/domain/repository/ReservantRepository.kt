package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Reservant
import kotlinx.coroutines.flow.Flow

/**
 * Contrat DIP pour la gestion des réservants.
 * Interface décrivant les opérations exposées par le repository
 * sans imposer de mécanisme de stockage local particulier.
 */
interface ReservantRepository {
    /**
     * Récupère le flux des réservants depuis la source de données.
     * Observable - se met à jour automatiquement lors de changements.
     */
    fun getAll(): Flow<List<Reservant>>

    /**
     * Synchronise les réservants depuis la source de données distante.
     * Peut échouer si offline et pas de cache local.
     */
    suspend fun refresh(): Result<Unit>

    /**
     * Récupère un réservant spécifique par ID.
     */
    suspend fun getById(reservantId: Int): Result<Reservant>

    /**
     * Crée un nouveau réservant via la source de données.
     */
    suspend fun create(reservant: Reservant): Result<Unit>

    /**
     * Met à jour un réservant existant via la source de données.
     */
    suspend fun update(reservant: Reservant): Result<Unit>

    /**
     * Supprime un réservant.
     */
    suspend fun delete(reservant: Reservant): Result<Unit>
}
