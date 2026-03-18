package com.example.festivaljeumobile.di

import com.example.festivaljeumobile.data.local.dao.JeuDao
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.repository.JeuRepositoryImpl
import com.example.festivaljeumobile.domain.repository.JeuRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Module Hilt pour l'injection des dépendances de la feature Jeux
 * Suit le pattern DIP : expose toujours l'interface, jamais l'implémentation
 */
@Module
@InstallIn(SingletonComponent::class)
object JeuModule {

    /**
     * Fournit l'interface JeuApi (service Retrofit)
     * @param retrofit Instance singleton fournie par NetworkModule
     */
    @Provides
    @Singleton
    fun provideJeuApi(retrofit: Retrofit): JeuApi =
        retrofit.create(JeuApi::class.java)

    /**
     * Fournit l'implémentation JeuRepository
     * Les ViewModels vont dépendre de l'interface JeuRepository, pas de l'implémentation
     * @param jeuDao DAO Room injecté par DatabaseModule
     * @param jeuApi API Retrofit injectée ci-dessus
     */
    @Provides
    @Singleton
    fun provideJeuRepository(
        jeuDao: JeuDao,
        jeuApi: JeuApi
    ): JeuRepository =
        JeuRepositoryImpl(jeuDao, jeuApi)
}
