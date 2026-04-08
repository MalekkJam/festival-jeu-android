package com.example.festivaljeumobile.di

import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.remote.api.UserApi
import com.example.festivaljeumobile.data.repository.JeuRepositoryImpl
import com.example.festivaljeumobile.data.repository.UserRepositoryImpl
import com.example.festivaljeumobile.data.service.AuthService
import com.example.festivaljeumobile.domain.repository.JeuRepository
import com.example.festivaljeumobile.domain.repository.UserRepository
import com.example.festivaljeumobile.viewModel.jeu.JeuFormViewModel
import com.example.festivaljeumobile.viewModel.jeu.JeuListViewModel
import com.example.festivaljeumobile.viewModel.user.UserFormViewModel
import com.example.festivaljeumobile.viewModel.user.UserListViewModel

object ServiceLocator {

    private val jeuApi: JeuApi by lazy {
        RetrofitInstance.retrofit.create(JeuApi::class.java)
    }

    private val userApi: UserApi by lazy {
        RetrofitInstance.retrofit.create(UserApi::class.java)
    }

    private val jeuRepository: JeuRepository by lazy {
        JeuRepositoryImpl(jeuApi)
    }

    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userApi)
    }

    fun createJeuListViewModel(): JeuListViewModel {
        return JeuListViewModel(jeuRepository)
    }

    fun createJeuFormViewModel(): JeuFormViewModel {
        return JeuFormViewModel(jeuRepository)
    }
}
