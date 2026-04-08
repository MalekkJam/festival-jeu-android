package com.example.festivaljeumobile.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.festivaljeumobile.domain.model.ZoneTarifaire

@Entity(
    tableName = "zone_tarifaires",
    foreignKeys = [
        ForeignKey(
            entity = FestivalEntity::class,
            parentColumns = ["id"],
            childColumns = ["festivalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["festivalId"])]
)
data class ZoneTarifaireEntity(
    @PrimaryKey val id: Int,
    val festivalId: Long,
    val nom: String,
    val nbTables: Int,
    val prixDuM2: Int,
) {
    fun toDomain(): ZoneTarifaire =
        ZoneTarifaire(
            id = id,
            nom = nom,
            nbTables = nbTables,
            prixDuM2 = prixDuM2
        )
}
