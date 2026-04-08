package com.example.festivaljeumobile.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import kotlinx.serialization.Serializable

@Serializable object Login : NavKey
@Serializable object Festivals : NavKey
@Serializable
data class FestivalForm(
    val id: Long? = null,
    val nom: String = "",
    val date_debut: String = "",
    val date_fin: String = "",
    val nbTables: Int = 0,
    val zoneTarifaires: List<ZoneTarifaire> = emptyList(),
) : NavKey
@Serializable
data class FestivalDetails(
    val id: Long? = null,
    val nom: String = "",
    val date_debut: String = "",
    val date_fin: String = "",
    val nbTables: Int = 0,
    val zoneTarifaires: List<ZoneTarifaire> = emptyList(),
) : NavKey
@Serializable object Jeux : NavKey
@Serializable object JeuList : NavKey
@Serializable data class JeuDetail(val jeuId: Int) : NavKey
@Serializable object JeuForm : NavKey
@Serializable data class JeuEditForm(val jeuId: Int) : NavKey
@Serializable object Reservations : NavKey
@Serializable data class ReservationForm(
    val reservation: Reservation? = null,
) : NavKey
@Serializable object Benevoles : NavKey
@Serializable object Editeurs : NavKey
@Serializable object Reservants : NavKey
@Serializable object Admin : NavKey

// Destinations gestion des utilisateurs (Admin uniquement)
@Serializable object UserList : NavKey
@Serializable object UserCreate : NavKey
@Serializable data class UserEdit(
    val id: Long,
    val login: String,
    val prenom: String = "",
    val nom: String = "",
    val role: UserRole = UserRole.Benevole,
) : NavKey

@Serializable object Logout : NavKey