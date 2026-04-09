package com.example.festivaljeumobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.local.dao.JeuDao
import com.example.festivaljeumobile.data.local.dao.ReservantDao
import com.example.festivaljeumobile.data.local.dao.ReservationDao
import com.example.festivaljeumobile.data.local.dao.ZoneTarifaireDao
import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import com.example.festivaljeumobile.data.local.entity.JeuEntity
import com.example.festivaljeumobile.data.local.entity.ReservantEntity
import com.example.festivaljeumobile.data.local.entity.ReservationEntity
import com.example.festivaljeumobile.data.local.entity.ZoneTarifaireEntity

@Database(
    entities = [FestivalEntity::class, ZoneTarifaireEntity::class, ReservationEntity::class, JeuEntity::class, ReservantEntity::class],
    version = 11,
    exportSchema = true
)
@TypeConverters(ZoneTarifaireConverters::class, ReservationConverters::class)
abstract class FestivalDatabase : RoomDatabase() {
    abstract fun festivalDao(): FestivalDao
    abstract fun zoneTarifaireDao(): ZoneTarifaireDao
    abstract fun reservationDao(): ReservationDao
    abstract fun jeuDao(): JeuDao
    abstract fun reservantDao(): ReservantDao
}
