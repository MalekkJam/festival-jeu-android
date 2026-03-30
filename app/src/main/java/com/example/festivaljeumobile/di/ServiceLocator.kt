package com.example.festivaljeumobile.di

import com.example.festivaljeumobile.data.repository.JeuRepositoryImpl
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.domain.repository.JeuRepository
import com.example.festivaljeumobile.ui.screens.jeu.JeuListViewModel
import com.example.festivaljeumobile.ui.screens.jeu.JeuFormViewModel
import com.example.festivaljeumobile.data.service.AuthService

object ServiceLocator {
    private val jeuApi: JeuApi by lazy {
        RetrofitInstance.retrofit.create(JeuApi::class.java)
    }

    private val jeuRepository: JeuRepository by lazy {
        JeuRepositoryImpl(jeuApi)
    }

    fun createJeuListViewModel(): JeuListViewModel {
        return JeuListViewModel(jeuRepository)
    }

    fun createJeuFormViewModel(): JeuFormViewModel {
        return JeuFormViewModel(jeuRepository)
    }

    fun getAuthService(): AuthService {
        return AuthService.getInstance()
    }
}
