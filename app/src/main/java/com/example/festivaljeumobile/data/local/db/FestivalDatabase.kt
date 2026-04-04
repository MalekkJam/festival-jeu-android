package com.example.festivaljeumobile.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.festivaljeumobile.data.local.dao.FestivalDao
import com.example.festivaljeumobile.data.local.entity.FestivalEntity

@Database(
    entities = [FestivalEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FestivalDatabase : RoomDatabase() {
    abstract fun festivalDao(): FestivalDao
}
