package com.example.festivaljeumobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivaljeumobile.domain.model.Festival

@Entity(tableName = "festivals")
data class FestivalEntity(
    @PrimaryKey val id: Long,
    val nom: String,
    val date_debut: String,
    val date_fin: String,
    val nbTables: Int,
) {
    fun toFestival(): Festival =
        Festival(
            id = id,
            nom = nom,
            date_debut = date_debut,
            date_fin = date_fin,
            nbTables = nbTables
        )
}
