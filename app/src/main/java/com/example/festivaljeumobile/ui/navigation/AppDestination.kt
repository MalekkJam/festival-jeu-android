package com.example.festivaljeumobile.ui.navigation

import androidx.navigation3.runtime.NavKey
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
) : NavKey
@Serializable object Jeux : NavKey
@Serializable object Reservations : NavKey
@Serializable object Benevoles : NavKey
@Serializable object Editeurs : NavKey
@Serializable object Reservants : NavKey
@Serializable object Admin : NavKey
@Serializable object Logout : NavKey
