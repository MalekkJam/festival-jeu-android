package com.example.festivaljeumobile.data.repository

import java.io.IOException

/**
 * Exception levée lors d'une tentative de synchronisation réseau en mode offline.
 * Permet au ViewModel de distinguer les erreurs de connectivité.
 */
class OfflineException(message: String) : IOException(message)
