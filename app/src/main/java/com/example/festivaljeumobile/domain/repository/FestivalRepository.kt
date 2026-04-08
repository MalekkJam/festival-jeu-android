package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import kotlinx.coroutines.flow.Flow

interface FestivalRepository {
    fun getAll(): Flow<List<Festival>>
    suspend fun refresh(): Result<Unit>
    suspend fun getZonesForFestival(festivalId: Long): Result<List<ZoneTarifaire>>
    suspend fun create(festival: Festival): Result<Unit>
    suspend fun delete(festival: Festival): Result<Unit>
    suspend fun update(festival: Festival): Result<Unit>
}
