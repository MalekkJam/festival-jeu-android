package com.example.festivaljeumobile.data.mapper

import com.example.festivaljeumobile.data.local.entity.ReservantEntity
import com.example.festivaljeumobile.data.remote.dto.ReservantDto
import com.example.festivaljeumobile.domain.model.Reservant

fun ReservantDto.toDomain(): Reservant = Reservant(
    id = id,
    nom = nom,
    type = type,
)

fun Reservant.toDto(): ReservantDto = ReservantDto(
    id = id,
    nom = nom,
    type = type,
)

fun ReservantDto.toEntity(): ReservantEntity = ReservantEntity(
    id = id,
    nom = nom,
    type = type.name,
)

fun Reservant.toEntity(): ReservantEntity = ReservantEntity(
    id = id,
    nom = nom,
    type = type.name,
)

fun List<ReservantDto>.toDomain(): List<Reservant> = map { it.toDomain() }

fun List<ReservantEntity>.asDomain(): List<Reservant> = map { it.toDomain() }

fun List<ReservantDto>.toEntity(): List<ReservantEntity> = map { it.toEntity() }
