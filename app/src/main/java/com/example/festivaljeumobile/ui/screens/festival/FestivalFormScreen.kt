package com.example.festivaljeumobile.ui.screens.festival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.viewModel.festival.FestivalFormEvent
import com.example.festivaljeumobile.viewModel.festival.FestivalFormUiState
import com.example.festivaljeumobile.viewModel.festival.FestivalFormViewModel

@Composable
fun FestivalFormScreen(
    initialFestival: Festival? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val festivalFormViewModel: FestivalFormViewModel = viewModel()
    val uiState by festivalFormViewModel.uiState.collectAsState()

    LaunchedEffect(initialFestival?.id) {
        festivalFormViewModel.setInitialFestival(initialFestival)
    }

    LaunchedEffect(Unit) {
        festivalFormViewModel.events.collect { event ->
            when (event) {
                FestivalFormEvent.Saved -> onBackClick()
            }
        }
    }

    FestivalFormContent(
        uiState = uiState,
        onNomChange = festivalFormViewModel::onNomChange,
        onDateDebutChange = festivalFormViewModel::onDateDebutChange,
        onDateFinChange = festivalFormViewModel::onDateFinChange,
        onNbTablesChange = festivalFormViewModel::onNbTablesChange,
        onSubmit = festivalFormViewModel::submit,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun FestivalFormContent(
    uiState: FestivalFormUiState,
    onNomChange: (String) -> Unit,
    onDateDebutChange: (String) -> Unit,
    onDateFinChange: (String) -> Unit,
    onNbTablesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.isEditMode) "Modification d'un festival" else "Creation d'un festival",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.nom,
            onValueChange = onNomChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nom") },
            singleLine = true,
            enabled = !uiState.isSubmitting
        )

        OutlinedTextField(
            value = uiState.dateDebut,
            onValueChange = onDateDebutChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date de debut") },
            supportingText = { Text("Format : YYYY-MM-DD") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = uiState.dateFin,
            onValueChange = onDateFinChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date de fin") },
            supportingText = { Text("Format : YYYY-MM-DD") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = uiState.nbTables,
            onValueChange = onNbTablesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de tables") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() })
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text("Retour a la liste")
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 2.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (uiState.isEditMode) "Mettre a jour le festival" else "Creer le festival")
            }
        }
    }
}
