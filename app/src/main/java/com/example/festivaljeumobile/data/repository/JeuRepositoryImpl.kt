package com.example.festivaljeumobile.data.repository

import com.example.festivaljeumobile.data.mapper.toDomain
import com.example.festivaljeumobile.data.mapper.toDto
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implémentation du Repository Jeu
 * Utilise Retrofit directement (pas de Room pour l'instant)
 */
class JeuRepositoryImpl(
    private val jeuApi: JeuApi
) : JeuRepository {

    /**
     * Récupère tous les jeux depuis l'API
     */
    override fun getAllJeux(): Flow<List<Jeu>> = flowOf(emptyList())

    /**
     * Crée un jeu via API
     */
    override suspend fun addJeu(jeu: Jeu): Result<Jeu> = try {
        val createdDto = jeuApi.addJeu(jeu.toDto())
        Result.success(createdDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Met à jour un jeu via API
     */
    override suspend fun updateJeu(jeu: Jeu): Result<Jeu> = try {
        val updatedDto = jeuApi.updateJeu(jeu.toDto())
        Result.success(updatedDto.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Supprime un jeu via API
     */
    override suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit> = try {
        jeuApi.deleteJeu(JeuApi.DeleteJeuRequest(id = idJeu, nom = libelleJeu))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Récupère un jeu par ID via API
     */
    override suspend fun getJeuById(idJeu: Int): Result<Jeu?> = try {
        val all = jeuApi.getAllJeux()
        val jeu = all.find { it.idJeu == idJeu }?.toDomain()
        Result.success(jeu)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Rafraîchit les jeux depuis l'API
     */
    override suspend fun refreshJeux(): Result<Unit> = try {
        jeuApi.getAllJeux()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
