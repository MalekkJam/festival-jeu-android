package com.example.festivaldujeu.di

import android.content.Context
import com.example.festivaldujeu.data.local.AppDatabase
import com.example.festivaldujeu.data.local.dao.JeuDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour la gestion des dépendances Room/Database
 * Fournit les DAOs et la base de données comme singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fournit l'instance singleton AppDatabase
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        AppDatabase.getInstance(context)

    /**
     * Fournit le DAO Jeu depuis la base de données
     */
    @Provides
    @Singleton
    fun provideJeuDao(database: AppDatabase): JeuDao =
        database.jeuDao()

    // TODO: @Provides pour les autres DAOs
}
