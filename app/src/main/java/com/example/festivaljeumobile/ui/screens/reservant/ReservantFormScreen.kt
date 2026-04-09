package com.example.festivaljeumobile.ui.screens.reservant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.viewModel.reservant.ReservantFormEvent
import com.example.festivaljeumobile.viewModel.reservant.ReservantFormViewModel
import kotlinx.coroutines.delay

/**
 * Écran formulaire pour créer/éditer un réservant.
 * Composable pur avec injection manuelle du ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservantFormScreen(
    initialReservant: Reservant? = null,
    readOnly: Boolean = false,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reservantFormViewModel: ReservantFormViewModel = viewModel()
    val uiState by reservantFormViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialReservant?.id) {
        reservantFormViewModel.setInitialReservant(initialReservant)
    }

    LaunchedEffect(Unit) {
        reservantFormViewModel.events.collect { event ->
            when (event) {
                is ReservantFormEvent.Saved -> {
                    snackbarHostState.showSnackbar("Réservant sauvegardé avec succès.")
                    // Petit délai pour la perception utilisateur
                    kotlinx.coroutines.delay(500)
                    onBackClick()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Éditer un réservant" else "Ajouter un réservant"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Affichage des erreurs
            if (uiState.error != null) {
                item {
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Champs du formulaire
            item {
                OutlinedTextField(
                    value = uiState.nom,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onNomChange(it) },
                    label = { Text("Nom *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.prenom,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onPrenomChange(it) },
                    label = { Text("Prénom *") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.telephone,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onTelephoneChange(it) },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.entreprise,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onEntrepriseChange(it) },
                    label = { Text("Entreprise") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.adresse,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onAdresseChange(it) },
                    label = { Text("Adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.codePostal,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onCodePostalChange(it) },
                    label = { Text("Code postal") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.ville,
                    onValueChange = { if (!readOnly) reservantFormViewModel.onVilleChange(it) },
                    label = { Text("Ville") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true
                )
            }

            // Boutons d'action
            if (!readOnly) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler")
                        }

                        Button(
                            onClick = { reservantFormViewModel.saveReservant() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSubmitting
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .align(Alignment.CenterVertically),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text("Sauvegarder")
                        }
                    }
                }
            }
        }
    }
}
