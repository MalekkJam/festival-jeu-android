package com.example.festivaljeumobile.data.local.db

import androidx.room.TypeConverter
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import kotlinx.serialization.json.Json

class ZoneTarifaireConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromZoneTarifaires(zoneTarifaires: List<ZoneTarifaire>): String =
        json.encodeToString(zoneTarifaires)

    @TypeConverter
    fun toZoneTarifaires(value: String): List<ZoneTarifaire> =
        if (value.isBlank()) {
            emptyList()
        } else {
            json.decodeFromString(value)
        }
}
