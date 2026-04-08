package com.example.festivaljeumobile.ui.navigation

import androidx.navigation3.runtime.NavKey
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
@Serializable object Benevoles : NavKey
@Serializable object Editeurs : NavKey
@Serializable object Reservants : NavKey
@Serializable object Admin : NavKey
@Serializable object Logout : NavKey
