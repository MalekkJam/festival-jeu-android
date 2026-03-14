package com.example.festivaldujeu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.festivaldujeu.data.local.dao.JeuDao
import com.example.festivaldujeu.data.local.entity.JeuEntity

/**
 * AppDatabase singleton Room
 * Centralise toutes les entités et DAOs (offline-first source of truth)
 * À mettre à jour lors de l'ajout de nouvelles features
 */
@Database(
    entities = [
        JeuEntity::class
        // TODO: Ajouter les autres entités ici à mesure qu'elles sont créées
        // Festival, Reservation, etc.
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jeuDao(): JeuDao
    // TODO: abstract fun festivalDao(): FestivalDao
    // TODO: abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "festival_jeu_db"
                )
                    .build()
                    .also { instance = it }
            }
    }
}
