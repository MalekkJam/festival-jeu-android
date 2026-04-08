package com.example.festivaljeumobile.ui.screens.jeu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.viewModel.jeu.JeuFormViewModel
import com.example.festivaljeumobile.viewModel.jeu.JeuDetailUiState
import com.example.festivaljeumobile.viewModel.jeu.JeuActionUiState

/**
 * Écran formulaire pour créer/éditer un jeu
 * Crée son propre ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuFormScreen(
    jeuId: Int? = null,
    onNavigateBack: () -> Unit = {},
    onSuccessNavigateBack: () -> Unit = {}
) {
    val viewModel: JeuFormViewModel = viewModel()
    val detailState = viewModel.detailUiState.collectAsState()
    val actionState = viewModel.actionUiState.collectAsState()

    // Champs du formulaire
    val libelle = remember { mutableStateOf("") }
    val auteur = remember { mutableStateOf("") }
    val nbMinJoueur = remember { mutableIntStateOf(0) }
    val nbMaxJoueur = remember { mutableIntStateOf(0) }
    val theme = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val duree = remember { mutableIntStateOf(0) }
    val agemini = remember { mutableIntStateOf(0) }

    // Charger le jeu au montage si édition
    LaunchedEffect(jeuId) {
        if (jeuId != null) {
            viewModel.loadJeu(jeuId)
        }
    }

    // Initialiser les champs quand les données arrivent (une seule fois par jeu)
    LaunchedEffect(detailState.value) {
        if (detailState.value is JeuDetailUiState.Success) {
            val jeu = (detailState.value as JeuDetailUiState.Success).jeu
            libelle.value = jeu.libelleJeu
            auteur.value = jeu.auteurJeu ?: ""
            nbMinJoueur.intValue = jeu.nbMinJoueurJeu ?: 0
            nbMaxJoueur.intValue = jeu.nbMaxJoueurJeu ?: 0
            theme.value = jeu.theme ?: ""
            description.value = jeu.description ?: ""
            duree.intValue = jeu.duree ?: 0
            agemini.intValue = jeu.agemini ?: 0
        }
    }

    // Gestion du succès d'une action (one-shot)
    LaunchedEffect(actionState.value) {
        if (actionState.value is JeuActionUiState.Success) {
            onSuccessNavigateBack()
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (jeuId == null) "Ajouter un jeu" else "Modifier un jeu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = detailState.value) {
            // Mode édition : chargement
            JeuDetailUiState.Loading -> {
                // Si c'est édition (jeuId != null), affiche un spinner
                if (jeuId != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Mode création : affiche le formulaire vide
                    FormContent(
                        libelle = libelle.value,
                        onLibelleChange = { libelle.value = it },
                        auteur = auteur.value,
                        onAuteurChange = { auteur.value = it },
                        nbMinJoueur = nbMinJoueur.intValue.toString(),
                        onNbMinJoueurChange = { nbMinJoueur.intValue = it.toIntOrNull() ?: 0 },
                        nbMaxJoueur = nbMaxJoueur.intValue.toString(),
                        onNbMaxJoueurChange = { nbMaxJoueur.intValue = it.toIntOrNull() ?: 0 },
                        theme = theme.value,
                        onThemeChange = { theme.value = it },
                        description = description.value,
                        onDescriptionChange = { description.value = it },
                        duree = duree.intValue.toString(),
                        onDureeChange = { duree.intValue = it.toIntOrNull() ?: 0 },
                        agemini = agemini.intValue.toString(),
                        onAgeMiniChange = { agemini.intValue = it.toIntOrNull() ?: 0 },
                        isLoading = actionState.value is JeuActionUiState.Loading,
                        error = (actionState.value as? JeuActionUiState.Error)?.message,
                        onSubmit = {
                            // Mode création : addJeu
                            val newJeu = Jeu(
                                idJeu = 0,
                                libelleJeu = libelle.value,
                                auteurJeu = auteur.value,
                                nbMinJoueurJeu = nbMinJoueur.intValue,
                                nbMaxJoueurJeu = nbMaxJoueur.intValue,
                                theme = theme.value,
                                description = description.value,
                                duree = duree.intValue,
                                agemini = agemini.intValue
                            )
                            viewModel.addJeu(newJeu)
                        },
                        onCancel = onNavigateBack,
                        paddingValues = paddingValues
                    )
                }
            }

            // Mode édition : jeu chargé avec succès
            is JeuDetailUiState.Success -> {
                val jeu = state.jeu
                FormContent(
                    libelle = libelle.value,
                    onLibelleChange = { libelle.value = it },
                    auteur = auteur.value,
                    onAuteurChange = { auteur.value = it },
                    nbMinJoueur = nbMinJoueur.intValue.toString(),
                    onNbMinJoueurChange = { nbMinJoueur.intValue = it.toIntOrNull() ?: 0 },
                    nbMaxJoueur = nbMaxJoueur.intValue.toString(),
                    onNbMaxJoueurChange = { nbMaxJoueur.intValue = it.toIntOrNull() ?: 0 },
                    theme = theme.value,
                    onThemeChange = { theme.value = it },
                    description = description.value,
                    onDescriptionChange = { description.value = it },
                    duree = duree.intValue.toString(),
                    onDureeChange = { duree.intValue = it.toIntOrNull() ?: 0 },
                    agemini = agemini.intValue.toString(),
                    onAgeMiniChange = { agemini.intValue = it.toIntOrNull() ?: 0 },
                    isLoading = actionState.value is JeuActionUiState.Loading,
                    error = (actionState.value as? JeuActionUiState.Error)?.message,
                    onSubmit = {
                        // Mode édition : updateJeu
                        val updatedJeu = jeu.copy(
                            libelleJeu = libelle.value,
                            auteurJeu = auteur.value,
                            nbMinJoueurJeu = nbMinJoueur.intValue,
                            nbMaxJoueurJeu = nbMaxJoueur.intValue,
                            theme = theme.value,
                            description = description.value,
                            duree = duree.intValue,
                            agemini = agemini.intValue
                        )
                        viewModel.updateJeu(updatedJeu)
                    },
                    onCancel = onNavigateBack,
                    paddingValues = paddingValues
                )
            }

            JeuDetailUiState.NotFound -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Jeu non trouvé")
                }
            }

            is JeuDetailUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Erreur: ${state.message}")
                }
            }
        }
    }
}

/**
 * Contenu du formulaire réutilisable
 */
@Composable
fun FormContent(
    libelle: String,
    onLibelleChange: (String) -> Unit,
    auteur: String,
    onAuteurChange: (String) -> Unit,
    nbMinJoueur: String,
    onNbMinJoueurChange: (String) -> Unit,
    nbMaxJoueur: String,
    onNbMaxJoueurChange: (String) -> Unit,
    theme: String,
    onThemeChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    duree: String,
    onDureeChange: (String) -> Unit,
    agemini: String,
    onAgeMiniChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Libellé
        OutlinedTextField(
            value = libelle,
            onValueChange = onLibelleChange,
            label = { Text("Libellé *") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Auteur
        OutlinedTextField(
            value = auteur,
            onValueChange = onAuteurChange,
            label = { Text("Auteur") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Nombre min joueurs
        OutlinedTextField(
            value = nbMinJoueur,
            onValueChange = onNbMinJoueurChange,
            label = { Text("Min joueurs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Nombre max joueurs
        OutlinedTextField(
            value = nbMaxJoueur,
            onValueChange = onNbMaxJoueurChange,
            label = { Text("Max joueurs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Thème
        OutlinedTextField(
            value = theme,
            onValueChange = onThemeChange,
            label = { Text("Thème") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Durée
        OutlinedTextField(
            value = duree,
            onValueChange = onDureeChange,
            label = { Text("Durée (min)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Âge minimum
        OutlinedTextField(
            value = agemini,
            onValueChange = onAgeMiniChange,
            label = { Text("Âge minimum") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 3
        )

        // Affichage erreur
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Boutons
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Enregistrer")
            }

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuler")
            }
        }
    }
}
