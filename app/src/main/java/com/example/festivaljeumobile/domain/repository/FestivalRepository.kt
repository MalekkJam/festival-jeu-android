package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.Festival
import kotlinx.coroutines.flow.Flow

interface FestivalRepository {
    fun getAll(): Flow<List<Festival>>
    suspend fun refresh(): Result<Unit>
    suspend fun create(festival: Festival): Result<Unit>
    suspend fun delete(festival: Festival): Result<Unit>
    suspend fun update(festival: Festival): Result<Unit>
}
