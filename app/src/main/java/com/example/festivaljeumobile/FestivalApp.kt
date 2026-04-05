package com.example.festivaljeumobile

import android.app.Application
import androidx.room.Room
import com.example.festivaljeumobile.data.local.db.FestivalDatabase
import com.example.festivaljeumobile.data.remote.RetrofitInstance
import com.example.festivaljeumobile.data.remote.api.FestivalApi
import com.example.festivaljeumobile.data.repository.FestivalRepositoryImpl

class FestivalApp : Application() {


    // used only to collect the dependencies in one place and to initialize them
    // by keeping the MainActivity file clean

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

    val festivalRepository by lazy {
        FestivalRepositoryImpl(
            festivalDao = festivalDatabase.festivalDao(),
            zoneTarifaireDao = festivalDatabase.zoneTarifaireDao(),
            festivalApi = festivalApi
        )
    }
}
