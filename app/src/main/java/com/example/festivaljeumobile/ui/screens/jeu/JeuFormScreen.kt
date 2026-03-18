package com.example.festivaljeumobile.ui.screens.jeu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.coerceIn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.festivaljeumobile.domain.model.Jeu

/**
 * Écran formulaire pour créer/éditer un jeu
 * Pattern MVVM strict : aucune logique métier, uniquement orchestration UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeuFormScreen(
    jeuId: Int? = null,
    viewModel: JeuFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onSuccessNavigateBack: () -> Unit = {}
) {
    val detailState = viewModel.detailUiState.collectAsState()
    val actionState = viewModel.actionUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Champs du formulaire (mutable state)
    val libelle = remember { mutableStateOf("") }
    val auteur = remember { mutableStateOf("") }
    val nbMinJoueur = remember { mutableIntStateOf(0) }
    val nbMaxJoueur = remember { mutableIntStateOf(0) }
    val notice = remember { mutableStateOf("") }
    val agemini = remember { mutableIntStateOf(0) }
    val prototype = remember { mutableStateOf(false) }
    val duree = remember { mutableIntStateOf(0) }
    val theme = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val videoRegle = remember { mutableStateOf("") }
    val idEditeur = remember { mutableIntStateOf(0) }
    val idTypeJeu = remember { mutableIntStateOf(0) }

    // Gestion des actions (feedback utilisateur)
    when (val state = actionState.value) {
        is JeuActionUiState.Success -> {
            // TODO: Afficher snackbar et naviguer
            onSuccessNavigateBack()
        }

        is JeuActionUiState.Error -> {
            // TODO: Afficher snackbar d'erreur
        }

        else -> {}
    }

    // Charger le jeu au montage si édition
    if (jeuId != null) {
        remember { viewModel.loadJeu(jeuId) }
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val detailState = detailState.value) {
            JeuDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is JeuDetailUiState.Success -> {
                val jeu = detailState.jeu
                // Remplir les champs avec les données du jeu
                libelle.value = jeu.libelleJeu
                auteur.value = jeu.auteurJeu ?: ""
                nbMinJoueur.intValue = jeu.nbMinJoueurJeu ?: 0
                nbMaxJoueur.intValue = jeu.nbMaxJoueurJeu ?: 0
                notice.value = jeu.noticeJeu ?: ""
                agemini.intValue = jeu.agemini ?: 0
                prototype.value = jeu.prototype ?: false
                duree.intValue = jeu.duree ?: 0
                theme.value = jeu.theme ?: ""
                description.value = jeu.description ?: ""
                videoRegle.value = jeu.videoRegle ?: ""
                idEditeur.intValue = jeu.idEditeur ?: 0
                idTypeJeu.intValue = jeu.idTypeJeu ?: 0

                JeuFormContent(
                    libelle = libelle.value,
                    onLabelleChange = { libelle.value = it },
                    auteur = auteur.value,
                    onAuteurChange = { auteur.value = it },
                    nbMinJoueur = nbMinJoueur.intValue,
                    onNbMinJoueurChange = { nbMinJoueur.intValue = it.coerceIn(0, 99) },
                    nbMaxJoueur = nbMaxJoueur.intValue,
                    onNbMaxJoueurChange = { nbMaxJoueur.intValue = it.coerceIn(0, 99) },
                    notice = notice.value,
                    onNoticeChange = { notice.value = it },
                    agemini = agemini.intValue,
                    onAgeMiniChange = { agemini.intValue = it.coerceIn(0, 99) },
                    theme = theme.value,
                    onThemeChange = { theme.value = it },
                    description = description.value,
                    onDescriptionChange = { description.value = it },
                    videoRegle = videoRegle.value,
                    onVideoRegleChange = { videoRegle.value = it },
                    imageUri = imageUri.value,
                    onImageSelected = { imageUri.value = it },
                    actionState = actionState.value,
                    onSubmit = {
                        val updatedJeu = jeu.copy(
                            libelleJeu = libelle.value,
                            auteurJeu = auteur.value,
                            nbMinJoueurJeu = nbMinJoueur.intValue,
                            nbMaxJoueurJeu = nbMaxJoueur.intValue,
                            noticeJeu = notice.value,
                            agemini = agemini.intValue,
                            prototype = prototype.value,
                            duree = duree.intValue,
                            theme = theme.value,
                            description = description.value,
                            videoRegle = videoRegle.value
                        )
                        viewModel.updateJeu(updatedJeu)
                    },
                    onCancel = onNavigateBack,
                    paddingValues = paddingValues
                )
            }

            JeuDetailUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Jeu non trouvé")
                }
            }

            is JeuDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Erreur : ${detailState.message}")
                }
            }
        }
    }
}

/**
 * Contenu du formulaire (réutilisable)
 */
@Composable
fun JeuFormContent(
    libelle: String,
    onLabelleChange: (String) -> Unit,
    auteur: String,
    onAuteurChange: (String) -> Unit,
    nbMinJoueur: Int,
    onNbMinJoueurChange: (Int) -> Unit,
    nbMaxJoueur: Int,
    onNbMaxJoueurChange: (Int) -> Unit,
    notice: String,
    onNoticeChange: (String) -> Unit,
    agemini: Int,
    onAgeMiniChange: (Int) -> Unit,
    theme: String,
    onThemeChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    videoRegle: String,
    onVideoRegleChange: (String) -> Unit,
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    actionState: JeuActionUiState,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Champ Libellé (obligatoire)
        OutlinedTextField(
            value = libelle,
            onValueChange = onLabelleChange,
            label = { Text("Libellé du jeu*") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Champ Auteur
        OutlinedTextField(
            value = auteur,
            onValueChange = onAuteurChange,
            label = { Text("Auteur") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Nombre de joueurs min
        OutlinedTextField(
            value = nbMinJoueur.toString(),
            onValueChange = { onNbMinJoueurChange(it.toIntOrNull() ?: 0) },
            label = { Text("Nombre min de joueurs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Nombre de joueurs max
        OutlinedTextField(
            value = nbMaxJoueur.toString(),
            onValueChange = { onNbMaxJoueurChange(it.toIntOrNull() ?: 0) },
            label = { Text("Nombre max de joueurs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Âge minimum
        OutlinedTextField(
            value = agemini.toString(),
            onValueChange = { onAgeMiniChange(it.toIntOrNull() ?: 0) },
            label = { Text("Âge minimum") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Thème
        OutlinedTextField(
            value = theme,
            onValueChange = onThemeChange,
            label = { Text("Thème") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            minLines = 3
        )

        // Champ Durée
        OutlinedTextField(
            value = nbMinJoueur.toString(),
            onValueChange = { onNbMinJoueurChange(it.toIntOrNull() ?: 0) },
            label = { Text("Durée (minutes)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Champ Règle vidéo
        OutlinedTextField(
            value = videoRegle,
            onValueChange = onVideoRegleChange,
            label = { Text("URL de la vidéo de règle") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true
        )

        // Sélecteur d'image
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Sélectionner une image")
        }

        // Aperçu d'image
        imageUri?.let { uri ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Aperçu",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Indicateur d'action
        when (actionState) {
            JeuActionUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )
            }

            is JeuActionUiState.Error -> {
                Text(
                    text = "Erreur: ${actionState.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            else -> {}
        }

        // Boutons d'action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = actionState != JeuActionUiState.Loading && libelle.isNotEmpty()
            ) {
                Text("Enregistrer")
            }

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                enabled = actionState != JeuActionUiState.Loading
            ) {
                Text("Annuler")
            }
        }
    }
}
