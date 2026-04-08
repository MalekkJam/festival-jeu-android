package com.example.festivaljeumobile.data.repository

import android.util.Log
import com.example.festivaljeumobile.data.local.dao.JeuDao
import com.example.festivaljeumobile.data.local.entity.JeuEntity
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

class JeuRepositoryImpl(
    private val jeuApi: JeuApi,
    private val jeuDao: JeuDao,
) : JeuRepository {

    override fun getAllJeux(): Flow<List<Jeu>> =
        jeuDao
            .observeAll()
            .map { jeux -> jeux.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addJeu(jeu: Jeu): Result<Jeu> =
        withContext(Dispatchers.IO) {
            try {
                val createdJeu = jeuApi.addJeu(jeu.toDto()).toDomain()
                jeuDao.upsert(createdJeu.toEntity())
                Result.success(createdJeu)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateJeu(jeu: Jeu): Result<Jeu> =
        withContext(Dispatchers.IO) {
            try {
                val updatedJeu = jeuApi.updateJeu(jeu.toDto()).toDomain()
                jeuDao.upsert(updatedJeu.toEntity())
                Result.success(updatedJeu)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteJeu(idJeu: Int, libelleJeu: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                jeuApi.deleteJeu(JeuApi.DeleteJeuRequest(id = idJeu, nom = libelleJeu))
                jeuDao.deleteById(idJeu)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getJeuById(idJeu: Int): Result<Jeu?> =
        withContext(Dispatchers.IO) {
            try {
                val localJeu = jeuDao.getById(idJeu)?.toDomain()
                if (localJeu != null) {
                    Result.success(localJeu)
                } else {
                    val remoteJeu = jeuApi.getAllJeux().find { it.idJeu == idJeu }?.toDomain()
                    if (remoteJeu != null) {
                        jeuDao.upsert(remoteJeu.toEntity())
                    }
                    Result.success(remoteJeu)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun refreshJeux(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d("JeuRepository", "refreshJeux() called - appel API")
                val jeux = jeuApi.getAllJeux().map { it.toDomain() }
                Log.d("JeuRepository", "API response : ${jeux.size} jeux recus")
                jeuDao.replaceAll(jeux.map { it.toEntity() })
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Log.e("JeuRepository", "Error refreshing jeux: ${throwable.message}", throwable)
                Result.failure(
                    when (throwable) {
                        is IOException -> OfflineException()
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de recuperer les jeux.")
                    }
                )
            }
        }
}

private fun Jeu.toEntity(): JeuEntity =
    JeuEntity(
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
        videoRegle = videoRegle,
    )
