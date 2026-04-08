package com.example.festivaljeumobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.local.dao.ZoneTarifaireDao
import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import com.example.festivaljeumobile.data.local.entity.ZoneTarifaireEntity

@Database(
    entities = [FestivalEntity::class, ZoneTarifaireEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(ZoneTarifaireConverters::class)
abstract class FestivalDatabase : RoomDatabase() {
    abstract fun festivalDao(): FestivalDao
    abstract fun zoneTarifaireDao(): ZoneTarifaireDao
}
