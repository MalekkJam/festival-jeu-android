package com.example.festivaljeumobile.data.repository

import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.remote.api.FestivalApi
import com.example.festivaljeumobile.data.remote.dto.toDeleteRequestDto
import com.example.festivaljeumobile.data.remote.dto.toDto
import com.example.festivaljeumobile.data.remote.dto.toEntity
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.repository.FestivalRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class FestivalRepositoryImpl(
    private val festivalDao: FestivalDao,
    private val festivalApi: FestivalApi,
) : FestivalRepository {

    override fun getAll(): Flow<List<Festival>> =
        festivalDao
            .observeAll()
            .map { festivals -> festivals.map { it.toFestival() } }
            .flowOn(Dispatchers.IO)

    override suspend fun refresh(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val remoteFestivals = festivalApi.getAllFestivals()
                festivalDao.upsertAll(remoteFestivals.map { it.toEntity() })
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> OfflineException()
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de recuperer les festivals.")
                    }
                )
            }
        }

    override suspend fun create(festival: Festival): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val createdFestival = festivalApi.addFestival(festival.toDto())
                festivalDao.upsertAll(listOf(createdFestival.toEntity()))
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Creation impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de creer le festival.")
                    }
                )
            }
        }

    override suspend fun delete(festival: Festival): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                festivalApi.deleteFestival(festival.toDeleteRequestDto())
                festivalDao.deleteById(festival.id)
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Suppression impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de supprimer le festival.")
                    }
                )
            }
        }

    override suspend fun update(festival: Festival): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updatedFestival = festivalApi.updateFestival(festival.toDto())
                festivalDao.upsertAll(listOf(updatedFestival.toEntity()))
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Modification impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de modifier le festival.")
                    }
                )
            }
        }

}

class OfflineException : IOException("Mode hors ligne : affichage des festivals en cache.")
