package com.example.festivaljeumobile.domain.repository

import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole

/**
 * Contrat DIP pour la gestion des utilisateurs.
 * Aucune dépendance vers les couches Data ou UI.
 * Accessible uniquement aux Admins (vérifié côté serveur).
 */
interface UserRepository {

    /**
     * Récupère tous les utilisateurs non-Admin.
     */
    suspend fun getAllUsers(): Result<List<User>>

    /**
     * Crée un nouvel utilisateur.
     */
    suspend fun addUser(
        login: String,
        password: String,
        prenom: String?,
        nom: String?,
        role: UserRole,
    ): Result<User>

    /**
     * Met à jour un utilisateur existant.
     */
    suspend fun updateUser(
        id: Long,
        login: String,
        prenom: String?,
        nom: String?,
        role: UserRole,
        password: String?,
    ): Result<User>

    /**
     * Supprime un utilisateur par son ID.
     */
    suspend fun deleteUser(id: Long): Result<Unit>
}