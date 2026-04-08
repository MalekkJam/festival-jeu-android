package com.example.festivaljeumobile

import android.app.Application
import androidx.room.Room
import com.example.festivaljeumobile.data.local.db.FestivalDatabase
import com.example.festivaljeumobile.data.local.preferences.CookieDataStore
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.data.remote.api.FestivalApi
import com.example.festivaljeumobile.data.remote.api.JeuApi
import com.example.festivaljeumobile.data.remote.api.ReservationApi
import com.example.festivaljeumobile.data.remote.api.ReservantApi
import com.example.festivaljeumobile.data.repository.FestivalRepositoryImpl
import com.example.festivaljeumobile.data.repository.ReservationRepositoryImpl

class FestivalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(this)
    }

    // used only to collect the dependencies in one place and to initialize them
    // by keeping the MainActivity file clean

    val cookieDataStore: CookieDataStore by lazy {
        CookieDataStore(applicationContext)
    }

    val festivalDatabase: FestivalDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FestivalDatabase::class.java,
            "festival_mobile.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val festivalApi: FestivalApi by lazy {
        RetrofitInstance.retrofit.create(FestivalApi::class.java)
    }

    val reservationApi: ReservationApi by lazy {
        RetrofitInstance.retrofit.create(ReservationApi::class.java)
    }

    val reservantApi: ReservantApi by lazy {
        RetrofitInstance.retrofit.create(ReservantApi::class.java)
    }

    val jeuApi: JeuApi by lazy {
        RetrofitInstance.retrofit.create(JeuApi::class.java)
    }

    val festivalRepository by lazy {
        FestivalRepositoryImpl(
            festivalDao = festivalDatabase.festivalDao(),
            zoneTarifaireDao = festivalDatabase.zoneTarifaireDao(),
            festivalApi = festivalApi
        )
    }

    val reservationRepository by lazy {
        ReservationRepositoryImpl(
            reservationDao = festivalDatabase.reservationDao(),
            zoneTarifaireDao = festivalDatabase.zoneTarifaireDao(),
            reservationApi = reservationApi,
            reservantApi = reservantApi,
            festivalApi = festivalApi,
            jeuApi = jeuApi
        )
    }
}
