package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Jeu
import kotlinx.coroutines.flow.Flow

/**
 * Contrat DIP pour la gestion des jeux
 * Aucune dépendance vers les couches Data ou UI
 */
interface JeuRepository {
    /**
     * Récupère tous les jeux (source de vérité = Room local)
     * Émet automatiquement à chaque changement local
     */
    fun getAllJeux(): Flow<List<Jeu>>

    /**
     * Créé un jeu via API et le persiste en local
     */
    suspend fun addJeu(jeu: Jeu): Result<Jeu>

    /**
     * Met à jour un jeu via API et en local
     */
    suspend fun updateJeu(jeu: Jeu): Result<Jeu>

    /**
     * Supprime un jeu via API et en local
     */
    suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit>

    /**
     * Récupère un jeu par ID (cache local)
     */
    suspend fun getJeuById(idJeu: Int): Result<Jeu?>

    /**
     * Rafraîchit les jeux depuis l'API (merge avec Room)
     */
    suspend fun refreshJeux(): Result<Unit>
}
