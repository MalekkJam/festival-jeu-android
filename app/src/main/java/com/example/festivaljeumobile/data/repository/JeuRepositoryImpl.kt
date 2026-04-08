package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.local.dao.JeuDao
import com.example.festivaljeumobile.data.local.entity.toEntity
import com.example.festivaljeumobile.data.mapper.toDomain
import com.example.festivaljeumobile.data.mapper.toDto
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.repository.JeuRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Implémentation du Repository Jeu — Offline-First Architecture
 * Stratégie :
 * - Source locale unique de vérité : Room Database
 * - Les opérations (create/update/delete) modifient d'abord l'API, puis synchronisent Room
 * - Le refresh() charge depuis l'API et cache les données localement
 * - En cas de perte réseau, le cache local est utilisé (fallback)
 * - Flow observeAll() notifie automatiquement les observateurs des changements Room
 */
class JeuRepositoryImpl(
    private val jeuDao: JeuDao,
    private val jeuApi: JeuApi
) : JeuRepository {

    /**
     * Récupère tous les jeux depuis le cache local (Room)
     * Retourne un Flow qui notifie automatiquement les changements
     */
    override fun getAllJeux(): Flow<List<Jeu>> =
        jeuDao
            .observeAll()
            .map { entities -> entities.map { it.toJeu() } }
            .flowOn(Dispatchers.IO)

    /**
     * Rafraîchit les jeux depuis l'API et synchronise avec le cache local
     * Pattern : API → Room → Flow notify → UI update
     */
    override suspend fun refreshJeux(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "refreshJeux() - Fetching from API")
                val remoteDtos = jeuApi.getAllJeux()
                Log.d("JeuRepository", "refreshJeux() - API returned ${remoteDtos.size} jeux, syncing to Room")
                
                // Convertir DTOs en entités et insérer dans Room
                val entities = remoteDtos.map { it.toEntity() }
                jeuDao.upsertAll(entities)
                
                Log.d("JeuRepository", "refreshJeux() - Sync complete, Room will notify observers")
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "refreshJeux() failed", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> OfflineException()
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de récupérer les jeux.")
                    }
                )
            }
        }

    /**
     * Crée un jeu via API, puis synchronise avec Room
     */
    override suspend fun addJeu(jeu: Jeu): Result<Jeu> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "addJeu() - Creating via API")
                val createdDto = jeuApi.addJeu(jeu.toDto())
                val createdJeu = createdDto.toDomain()
                
                // Synchroniser dans Room
                jeuDao.upsertAll(listOf(createdDto.toEntity()))
                Log.d("JeuRepository", "addJeu() - Room synced, observers will be notified")
                
                Result.success(createdJeu)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "addJeu() failed", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Création impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de créer le jeu.")
                    }
                )
            }
        }

    /**
     * Met à jour un jeu via API, puis synchronise avec Room
     */
    override suspend fun updateJeu(jeu: Jeu): Result<Jeu> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "updateJeu(${jeu.idJeu}) - Updating via API")
                val updatedDto = jeuApi.updateJeu(jeu.toDto())
                val updatedJeu = updatedDto.toDomain()
                
                // Synchroniser dans Room
                jeuDao.upsertAll(listOf(updatedDto.toEntity()))
                Log.d("JeuRepository", "updateJeu(${jeu.idJeu}) - Room synced")
                
                Result.success(updatedJeu)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "updateJeu() failed", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Modification impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de modifier le jeu.")
                    }
                )
            }
        }

    /**
     * Supprime un jeu via API, puis supprime du cache Room
     * La suppression Room déclenche automatiquement une notification Flow → UI update
     */
    override suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "deleteJeu($idJeu) - Deleting via API")
                jeuApi.deleteJeu(JeuApi.DeleteJeuRequest(id = idJeu, nom = libelleJeu))
                
                // Supprimer du cache Room
                jeuDao.deleteById(idJeu)
                Log.d("JeuRepository", "deleteJeu($idJeu) - Removed from Room, observers will be notified")
                
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "deleteJeu() failed", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Suppression impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de supprimer le jeu.")
                    }
                )
            }
        }

    /**
     * Récupère un jeu par ID depuis le cache Room
     */
    override suspend fun getJeuById(idJeu: Int): Result<Jeu?> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "getJeuById($idJeu) - Fetching from Room cache")
                val jeuEntity = jeuDao.getById(idJeu)
                val jeu = jeuEntity?.toJeu()
                Result.success(jeu)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "getJeuById() failed", throwable)
                Result.failure(throwable)
            }
        }
}

// Extension pour convertir JeuDto → JeuEntity
private fun com.example.festivaljeumobile.data.remote.dto.JeuDto.toEntity() =
    com.example.festivaljeumobile.data.local.entity.JeuEntity(
        idJeu = idJeu,
        libelleJeu = libelleJeu,
        auteurJeu = auteurJeu,
        nbMinJoueurJeu = nbMinJoueurJeu,
        nbMaxJoueurJeu = nbMaxJoueurJeu,
        noticeJeu = noticeJeu,
        idEditeur = idEditeur,
        idTypeJeu = idTypeJeu,
        agemini = agemini,
        prototype = prototype,
        duree = duree,
        theme = theme,
        description = description,
        imageJeu = imageJeu,
        videoRegle = videoRegle
    )
