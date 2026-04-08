package com.example.festivaljeumobile.data.repository

import com.example.festivaljeumobile.data.local.dao.ReservationDao
import com.example.festivaljeumobile.data.local.dao.ZoneTarifaireDao
import com.example.festivaljeumobile.data.mapper.toDomain
import com.example.festivaljeumobile.data.remote.api.FestivalApi
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.remote.api.ReservationApi
import com.example.festivaljeumobile.data.remote.api.ReservantApi
import com.example.festivaljeumobile.data.remote.dto.toReservantOption
import com.example.festivaljeumobile.data.remote.dto.toSaveRequestDto
import com.example.festivaljeumobile.data.remote.dto.toZoneTarifaire
import com.example.festivaljeumobile.data.remote.dto.toEntity
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ReservantOption
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import com.example.festivaljeumobile.domain.repository.ReservationRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ReservationRepositoryImpl(
    private val reservationDao: ReservationDao,
    private val zoneTarifaireDao: ZoneTarifaireDao,
    private val reservationApi: ReservationApi,
    private val reservantApi: ReservantApi,
    private val festivalApi: FestivalApi,
    private val jeuApi: JeuApi,
) : ReservationRepository {

    override fun getAll(): Flow<List<Reservation>> =
        reservationDao
            .observeAll()
            .map { reservations -> reservations.map { it.toReservation() } }
            .flowOn(Dispatchers.IO)

    override suspend fun refresh(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val remoteReservations = reservationApi.getAllReservations()
                reservationDao.replaceAll(remoteReservations.map { it.toEntity() })
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> OfflineException()
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de recuperer les reservations.")
                    }
                )
            }
        }

    override suspend fun getReservants(): Result<List<ReservantOption>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(reservantApi.getAllReservants().map { it.toReservantOption() })
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de charger les reservants hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de charger les reservants.")
                    }
                )
            }
        }

    override suspend fun getFestivals(): Result<List<Festival>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(festivalApi.getAllFestivals().map { it.toEntity().toFestival() })
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de charger les festivals hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de charger les festivals.")
                    }
                )
            }
        }

    override suspend fun getZonesForFestival(festivalId: Long): Result<List<ZoneTarifaire>> =
        withContext(Dispatchers.IO) {
            try {
                val remoteZones = festivalApi.getAllZones(festivalId).map { it.toZoneTarifaire() }
                if (remoteZones.isNotEmpty()) {
                    Result.success(remoteZones)
                } else {
                    val localZones = zoneTarifaireDao.getByFestivalId(festivalId).map { it.toDomain() }
                    Result.success(localZones)
                }
            } catch (throwable: Throwable) {
                val localZones = zoneTarifaireDao.getByFestivalId(festivalId).map { it.toDomain() }
                if (localZones.isNotEmpty()) {
                    Result.success(localZones)
                } else {
                    Result.failure(
                        when (throwable) {
                            is IOException -> Exception("Impossible de charger les zones hors ligne.")
                            is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                            else -> Exception("Impossible de charger les zones.")
                        }
                    )
                }
            }
        }

    override suspend fun getJeux(): Result<List<Jeu>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(jeuApi.getAllJeux().map { it.toDomain() })
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Impossible de charger les jeux hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de charger les jeux.")
                    }
                )
            }
        }

    override suspend fun create(reservation: Reservation): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val createdReservation = reservationApi.addReservation(reservation.toSaveRequestDto().copy(id = null))
                reservationDao.upsertAll(listOf(createdReservation.toEntity()))
                Result.success(Unit)
            } catch (throwable: Throwable) {
                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Creation impossible hors ligne.")
                        is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                        else -> Exception("Impossible de creer la reservation.")
                    }
                )
            }
        }

    override suspend fun update(reservation: Reservation): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (reservation.id == null || reservation.id <= 0L) {
                return@withContext Result.failure(Exception("Identifiant de reservation manquant."))
            }

            try {
                val updatedReservation = reservationApi.updateReservation(reservation.toSaveRequestDto())
                reservationDao.upsertAll(listOf(updatedReservation.toEntity()))
                Result.success(Unit)
            } catch (throwable: Throwable) {
                if (throwable is HttpException && throwable.code() == 404) {
                    reservation.id?.let { reservationDao.deleteById(it) }
                }

                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Modification impossible hors ligne.")
                        is HttpException -> {
                            if (throwable.code() == 404) {
                                Exception("Cette reservation n'existe plus sur le serveur.")
                            } else {
                                Exception("Erreur serveur (${throwable.code()}).")
                            }
                        }
                        else -> Exception("Impossible de modifier la reservation.")
                    }
                )
            }
        }

    override suspend fun delete(reservationId: Long): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                reservationApi.deleteReservation(reservationId)
                reservationDao.deleteById(reservationId)
                Result.success(Unit)
            } catch (throwable: Throwable) {
                if (throwable is HttpException && throwable.code() == 404) {
                    reservationDao.deleteById(reservationId)
                }

                Result.failure(
                    when (throwable) {
                        is IOException -> Exception("Suppression impossible hors ligne.")
                        is HttpException -> {
                            if (throwable.code() == 404) {
                                Exception("Cette reservation n'existe plus sur le serveur.")
                            } else {
                                Exception("Erreur serveur (${throwable.code()}).")
                            }
                        }
                        else -> Exception("Impossible de supprimer la reservation.")
                    }
                )
            }
        }
}
