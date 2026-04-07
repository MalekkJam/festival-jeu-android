package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Jeu
import kotlinx.coroutines.flow.Flow

/**
 * Contrat DIP pour la gestion des jeux.
 * Aucune dépendance vers les couches Data ou UI.
 *
 * Cette interface décrit les opérations exposées par le repository
 * sans imposer de mécanisme de stockage local particulier.
 */
interface JeuRepository {
    /**
     * Récupère le flux des jeux exposé par le repository.
     */
    fun getAllJeux(): Flow<List<Jeu>>

    /**
     * Crée un jeu via la source de données configurée par l'implémentation.
     */
    suspend fun addJeu(jeu: Jeu): Result<Jeu>

    /**
     * Met à jour un jeu via la source de données configurée par l'implémentation.
     */
    suspend fun updateJeu(jeu: Jeu): Result<Jeu>

    /**
     * Supprime un jeu via la source de données configurée par l'implémentation.
     */
    suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit>

    /**
     * Récupère un jeu par ID.
     */
    suspend fun getJeuById(idJeu: Int): Result<Jeu?>

    /**
     * Rafraîchit les jeux depuis la source distante selon l'implémentation.
     */
    suspend fun refreshJeux(): Result<Unit>
}
