package com.example.festivaljeumobile.data.repository

import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.remote.api.FestivalApi
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
}

class OfflineException : IOException("Mode hors ligne : affichage des festivals en cache.")
