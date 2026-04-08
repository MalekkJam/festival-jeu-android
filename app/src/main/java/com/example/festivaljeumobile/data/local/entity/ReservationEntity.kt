package com.example.festivaljeumobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ReservationJeu

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: Long,
    val reservantId: Int,
    val festivalId: Int,
    val festivalName: String,
    val reservantName: String,
    val etatDeSuivi: String,
    val dateDeContact: String? = null,
    val remise: String? = null,
    val montantRemise: Int? = null,
    val facturee: Boolean,
    val payee: Boolean,
    val jeux: List<ReservationJeu> = emptyList(),
) {
    fun toReservation(): Reservation =
        Reservation(
            id = id,
            reservantId = reservantId,
            festivalId = festivalId,
            festivalName = festivalName,
            reservantName = reservantName,
            etatDeSuivi = etatDeSuivi,
            dateDeContact = dateDeContact,
            remise = remise,
            montantRemise = montantRemise,
            facturee = facturee,
            payee = payee,
            jeux = jeux
        )
}
