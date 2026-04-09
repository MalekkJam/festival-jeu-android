package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.local.dao.ReservantDao
import com.example.festivaljeumobile.data.remote.api.ReservantApi
import com.example.festivaljeumobile.data.remote.dto.toDomain
import com.example.festivaljeumobile.data.remote.dto.toDto
import com.example.festivaljeumobile.data.remote.dto.toEntity
import com.example.festivaljeumobile.data.remote.dto.toDeleteRequestDto
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.repository.ReservantRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

private const val TAG = "ReservantRepository"

/**
 * Implémentation concrète du repository Réservant.
 * Orchestre l'API Retrofit + le cache local Room.
 * Pattern offline-first : essaie le cache local en premier.
 */
class ReservantRepositoryImpl(
    private val reservantDao: ReservantDao,
    private val reservantApi: ReservantApi,
) : ReservantRepository {

    /**
     * Expose un Flow des réservants depuis le cache local.
     * S'émet automatiquement lors de mises à jour en base.
     */
    override fun getAll(): Flow<List<Reservant>> =
        reservantDao
            .observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    /**
     * Récupère un réservant spécifique par ID.
     * Essaie d'abord le cache local, puis requête l'API.
     */
    override suspend fun getById(reservantId: Int): Result<Reservant> =
        withContext(Dispatchers.IO) {
            try {
                // Essaie le cache local en premier
                val localReservant = reservantDao.getById(reservantId)
                if (localReservant != null) {
                    return@withContext Result.success(localReservant.toDomain())
                }

                // Si pas en cache, cherche en remote
                val remoteReservant = reservantApi.getReservantById(reservantId)
                // Cache le résultat
                reservantDao.upsert(remoteReservant.toEntity())
                Result.success(remoteReservant.toDomain())
            } catch (throwable: Throwable) {
                Log.e(TAG, "Erreur lors de la récupération du réservant $reservantId", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de charger le réservant hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Erreur lors du chargement du réservant.")
                    }
                )
            }
        }

    /**
     * Synchronise les réservants depuis l'API distante vers le cache local.
     * Offre une stratégie offline-first.
     */
    override suspend fun refresh(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val remoteReservants = reservantApi.getAllReservants()
                // Populate/sync cache local
                syncReservantsCache(reservantDao, remoteReservants)
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e(TAG, "Erreur lors du refresh des réservants", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> OfflineException("Impossible de charger les réservants hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Erreur lors du refresh des réservants.")
                    }
                )
            }
        }

    /**
     * Crée un nouveau réservant via l'API.
     * Met également en cache local si succès.
     */
    override suspend fun create(reservant: Reservant): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val createdDto = reservantApi.addReservant(reservant.toDto())
                val createdReservant = createdDto.toDomain()
                // Cache le résultat avec le vrai id du serveur
                reservantDao.upsert(createdDto.toEntity())
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e(TAG, "Erreur lors de la création du réservant", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de créer le réservant hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception(throwable.message ?: "Impossible de creer le reservant.")
                    }
                )
            }
        }

    /**
     * Met à jour un réservant existant via l'API.
     * Synchronise également le cache local.
     */
    override suspend fun update(reservant: Reservant): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updatedDto = reservantApi.updateReservant(reservant.toDto())
                val updatedReservant = updatedDto.toDomain()
                // Synchronise le cache
                reservantDao.upsert(updatedDto.toEntity())
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e(TAG, "Erreur lors de la mise a jour du reservant ${reservant.id}", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de mettre a jour le reservant hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception(throwable.message ?: "Impossible de mettre a jour le reservant.")
                    }
                )
            }
        }

    /**
     * Supprime un réservant via l'API.
     * Supprime également du cache local si succès.
     */
    override suspend fun delete(reservant: Reservant): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                reservantApi.deleteReservant(reservant.toDeleteRequestDto())
                // Supprime du cache local
                val entity = reservantDao.getById(reservant.id)
                if (entity != null) {
                    reservantDao.delete(entity)
                }
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e(TAG, "Erreur lors de la suppression du réservant ${reservant.id}", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de supprimer le réservant hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Erreur lors de la suppression du réservant.")
                    }
                )
            }
        }
}

/**
 * Synchronise la liste des réservants distants vers le cache local.
 * Remplace complètement le cache existant.
 */
private suspend fun syncReservantsCache(
    reservantDao: ReservantDao,
    remoteReservants: List<com.example.festivaljeumobile.data.remote.dto.ReservantDto>
) {
    reservantDao.deleteAll()
    val entities = remoteReservants.toEntity()
    if (entities.isNotEmpty()) {
        reservantDao.upsertAll(entities)
    }
}
