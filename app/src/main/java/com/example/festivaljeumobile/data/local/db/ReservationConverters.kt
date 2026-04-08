package com.example.festivaljeumobile.data.local.db

import androidx.room.TypeConverter
import com.example.festivaljeumobile.domain.model.ReservationJeu
import kotlinx.serialization.json.Json

class ReservationConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromReservationJeux(jeux: List<ReservationJeu>): String =
        json.encodeToString(jeux)

    @TypeConverter
    fun toReservationJeux(value: String): List<ReservationJeu> =
        if (value.isBlank()) {
            emptyList()
        } else {
            json.decodeFromString(value)
        }
}
