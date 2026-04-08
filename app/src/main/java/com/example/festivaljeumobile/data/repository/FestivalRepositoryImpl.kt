package com.example.festivaljeumobile.data.repository

import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.local.dao.ZoneTarifaireDao
import com.example.festivaljeumobile.data.local.entity.ZoneTarifaireEntity
import com.example.festivaljeumobile.data.remote.api.FestivalApi
import com.example.festivaljeumobile.data.remote.dto.FestivalDto
import com.example.festivaljeumobile.data.remote.dto.ZoneTarifaireDto
import com.example.festivaljeumobile.data.remote.dto.toZoneTarifaire
import com.example.festivaljeumobile.data.remote.dto.toDeleteRequestDto
import com.example.festivaljeumobile.data.remote.dto.toDto
import com.example.festivaljeumobile.data.remote.dto.toEntity
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
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
    private val zoneTarifaireDao: ZoneTarifaireDao,
    private val festivalApi: FestivalApi,
) : FestivalRepository {

    override fun getAll(): Flow<List<Festival>> =
        festivalDao
            .observeAll()
            .map { festivals -> festivals.map { it.toFestival() } }
            .flowOn(Dispatchers.IO)

    override suspend fun getZonesForFestival(festivalId: Long): Result<List<ZoneTarifaire>> =
        withContext(Dispatchers.IO) {
            try {
                val remoteZones = festivalApi.getAllZones(festivalId)
                syncZonesForFestival(
                    zoneTarifaireDao = zoneTarifaireDao,
                    festivalId = festivalId,
                    zones = remoteZones
                )
                Result.success(remoteZones.map { it.toZoneTarifaire() })
            } catch (throwable: Throwable) {
                val localZones = zoneTarifaireDao.getByFestivalId(festivalId).map { it.toDomain() }
                if (localZones.isNotEmpty()) {
                    Result.success(localZones)
                }
                else {
                    Result.failure(
                        when (throwable) {
                            is IOException -> Exception("Impossible de charger les zones hors ligne.")
                            is HttpException -> Exception("Erreur serveur (${throwable.code()}).")
                            else -> Exception("Impossible de charger les zones tarifaires.")
                        }
                    )
                }
            }
        }

    override suspend fun refresh(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val remoteFestivals = festivalApi.getAllFestivals()
                val hydratedFestivals = remoteFestivals.map { festival ->
                    val festivalId = festival.id
                    val zones = if (festivalId != null) {
                        runCatching { festivalApi.getAllZones(festivalId) }
                            .getOrElse { festival.zones }
                    } else {
                        festival.zones
                    }
                    festival.copy(
                        zones = zones,
                        nbTables = zones.sumOf { it.nbTables }
                    )
                }

                festivalDao.upsertAll(hydratedFestivals.map { it.toEntity() })
                hydratedFestivals.filter { it.id != null }.forEach { festival ->
                    syncZonesForFestival(
                        zoneTarifaireDao = zoneTarifaireDao,
                        festivalId = festival.id!!,
                        zones = festival.zones
                    )
                }
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
                val createdFestival = festivalApi.addFestival(festival.toDtoForCreate())
                val syncedFestival = createdFestival.withFallbackFrom(festival).withComputedTables()
                festivalDao.upsertAll(listOf(syncedFestival.toEntity()))
                syncedFestival.id?.let { festivalId ->
                    syncZonesForFestival(
                        zoneTarifaireDao = zoneTarifaireDao,
                        festivalId = festivalId,
                        zones = syncedFestival.zones
                    )
                }
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
                zoneTarifaireDao.deleteByFestivalId(festival.id)
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
                festival.zoneTarifaires.forEach { zone ->
                    if (zone.id != null) {
                        festivalApi.updateZone(zone.id, zone.toDto())
                    } else {
                        festivalApi.addZone(festival.id, zone.toDto())
                    }
                }
                val updatedFestival = festivalApi.updateFestival(festival.toDto())
                val remoteZones = runCatching { festivalApi.getAllZones(festival.id) }
                    .getOrElse { festival.zoneTarifaires.map { it.toDto() } }
                val syncedFestival = updatedFestival
                    .withFallbackFrom(festival)
                    .copy(
                        zones = remoteZones,
                        nbTables = remoteZones.sumOf { it.nbTables }
                    )
                festivalDao.upsertAll(listOf(syncedFestival.toEntity()))
                syncZonesForFestival(
                    zoneTarifaireDao = zoneTarifaireDao,
                    festivalId = syncedFestival.id ?: festival.id,
                    zones = syncedFestival.zones
                )
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

private fun Festival.toDtoForCreate(): FestivalDto =
    toDto().copy(id = null)

private fun FestivalDto.withFallbackFrom(festival: Festival): FestivalDto =
    copy(
        id = id ?: festival.id,
        nbTables = nbTables ?: festival.nbTables,
        zones = if (zones.isEmpty()) festival.zoneTarifaires.map { zone ->
            ZoneTarifaireDto(
                id = zone.id,
                nom = zone.nom,
                nbTables = zone.nbTables,
                prixDuM2 = zone.prixDuM2
            )
        } else {
            zones
        }
    )

private suspend fun syncZonesForFestival(
    zoneTarifaireDao: ZoneTarifaireDao,
    festivalId: Long,
    zones: List<ZoneTarifaireDto>
) {
    zoneTarifaireDao.deleteByFestivalId(festivalId)
    val zoneEntities = zones.mapNotNull { zone ->
        zone.id?.let { zoneId ->
            ZoneTarifaireEntity(
                id = zoneId,
                festivalId = festivalId,
                nom = zone.nom,
                nbTables = zone.nbTables,
                prixDuM2 = zone.prixDuM2
            )
        }
    }
    if (zoneEntities.isNotEmpty()) {
        zoneTarifaireDao.upsertAll(zoneEntities)
    }
}

private fun FestivalDto.withComputedTables(): FestivalDto =
    copy(nbTables = nbTables ?: zones.sumOf { it.nbTables })
