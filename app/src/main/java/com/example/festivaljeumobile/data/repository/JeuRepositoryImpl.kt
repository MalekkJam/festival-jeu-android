package com.example.festivaldujeu.data.repository

import com.example.festivaldujeu.data.local.dao.JeuDao
import com.example.festivaldujeu.data.mapper.toDomain
import com.example.festivaldujeu.data.mapper.toDto
import com.example.festivaldujeu.data.mapper.toEntity
import com.example.festivaldujeu.data.remote.api.JeuApi
import com.example.festivaldujeu.domain.model.Jeu
import com.example.festivaldujeu.domain.repository.JeuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implémentation concrète du Repository Jeu
 * Pattern offline-first : Room est la source de vérité, API rafraîchit en arrière-plan
 * Cette couche est la SEULE autorisée à connaître Retrofit et Room
 */
class JeuRepositoryImpl @Inject constructor(
    private val jeuDao: JeuDao,
    private val jeuApi: JeuApi
) : JeuRepository {

    /**
     * Récupère tous les jeux depuis Room (source locale)
     * Émet automatiquement à chaque changement local
     */
    override fun getAllJeux(): Flow<List<Jeu>> =
        jeuDao.getAllJeux().map { entities ->
            entities.toDomain()
        }

    /**
     * Crée un jeu (API → Room)
     * @return Succès : Jeu créé avec son idJeu assigné par le backend
     */
    override suspend fun addJeu(jeu: Jeu): Result<Jeu> = try {
        // Appel API (idJeu ignoré, le backend génère)
        val createdDto = jeuApi.addJeu(jeu.toDto())
        val createdJeu = createdDto.toDomain()

        // Persiste localement
        jeuDao.insertJeu(createdJeu.toEntity())

        Result.success(createdJeu)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Met à jour un jeu (API → Room)
     */
    override suspend fun updateJeu(jeu: Jeu): Result<Jeu> = try {
        val updatedDto = jeuApi.updateJeu(jeu.toDto())
        val updatedJeu = updatedDto.toDomain()

        // Mise à jour locale
        jeuDao.updateJeu(updatedJeu.toEntity())

        Result.success(updatedJeu)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Supprime un jeu via API puis localement
     * Envoyer le nom du jeu (backend l'utilise pour logs)
     */
    override suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit> = try {
        // Appel API avec le nom
        jeuApi.deleteJeu(JeuApi.DeleteJeuRequest(id = idJeu, nom = libelleJeu))

        // Suppression locale
        jeuDao.deleteJeuById(idJeu)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Récupère un jeu par ID depuis le cache local
     */
    override suspend fun getJeuById(idJeu: Int): Result<Jeu?> = try {
        val entity = jeuDao.getJeuById(idJeu)
        Result.success(entity?.toDomain())
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Rafraîchit les jeux depuis l'API (merge offline-first)
     * Pattern : API → Room (replace avec UPSERT)
     * Utile après une reconnexion réseau
     */
    override suspend fun refreshJeux(): Result<Unit> = try {
        val dtos = jeuApi.getAllJeux()
        val entities = dtos.toDomain().map { it.toEntity() }

        // UPSERT : insère ou remplace (OnConflictStrategy.REPLACE)
        jeuDao.clearAll()
        jeuDao.insertAllJeux(entities)

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
