package com.example.festivaljeumobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.local.dao.JeuDao
import com.example.festivaljeumobile.data.local.dao.ZoneTarifaireDao
import com.example.festivaljeumobile.data.local.entity.FestivalEntity
import com.example.festivaljeumobile.data.local.entity.JeuEntity
import com.example.festivaljeumobile.data.local.entity.ZoneTarifaireEntity

@Database(
    entities = [FestivalEntity::class, ZoneTarifaireEntity::class, JeuEntity::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(ZoneTarifaireConverters::class)
abstract class FestivalDatabase : RoomDatabase() {
    abstract fun festivalDao(): FestivalDao
    abstract fun zoneTarifaireDao(): ZoneTarifaireDao
    abstract fun jeuDao(): JeuDao
}
