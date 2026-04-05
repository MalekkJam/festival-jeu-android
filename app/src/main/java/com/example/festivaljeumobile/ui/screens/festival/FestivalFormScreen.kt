package com.example.festivaljeumobile.ui.screens.festival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.viewModel.festival.FestivalFormEvent
import com.example.festivaljeumobile.viewModel.festival.FestivalFormUiState
import com.example.festivaljeumobile.viewModel.festival.FestivalFormViewModel
import com.example.festivaljeumobile.viewModel.festival.ZoneTarifaireFormUiState

@Composable
fun FestivalFormScreen(
    initialFestival: Festival? = null,
    readOnly: Boolean = false,
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
        onZoneCountChange = festivalFormViewModel::onZoneCountChange,
        onZoneNomChange = festivalFormViewModel::onZoneNomChange,
        onZoneNbTablesChange = festivalFormViewModel::onZoneNbTablesChange,
        onZonePrixDuM2Change = festivalFormViewModel::onZonePrixDuM2Change,
        onSubmit = festivalFormViewModel::submit,
        readOnly = readOnly,
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
    onZoneCountChange: (String) -> Unit,
    onZoneNomChange: (Int, String) -> Unit,
    onZoneNbTablesChange: (Int, String) -> Unit,
    onZonePrixDuM2Change: (Int, String) -> Unit,
    onSubmit: () -> Unit,
    readOnly: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = when {
                readOnly -> "Details du festival"
                uiState.isEditMode -> "Modification d'un festival"
                else -> "Creation d'un festival"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.nom,
            onValueChange = onNomChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nom") },
            singleLine = true,
            enabled = !uiState.isSubmitting && !readOnly,
            readOnly = readOnly
        )

        OutlinedTextField(
            value = uiState.dateDebut,
            onValueChange = onDateDebutChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date de debut") },
            supportingText = { Text("Format : YYYY-MM-DD") },
            singleLine = true,
            enabled = !uiState.isSubmitting && !readOnly,
            readOnly = readOnly,
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
            enabled = !uiState.isSubmitting && !readOnly,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = uiState.zoneCount,
            onValueChange = onZoneCountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de zones tarifaires") },
            singleLine = true,
            enabled = !uiState.isSubmitting && !readOnly,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        OutlinedTextField(
            value = uiState.totalNbTables.toString(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre total de tables") },
            readOnly = true,
            enabled = false,
            supportingText = { Text("Calcule automatiquement depuis les zones tarifaires") }
        )

        if (uiState.zonesTarifaires.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    text = "Pas de zone tarifaires pour l'instant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )}
        } else {
            uiState.zonesTarifaires.forEachIndexed { index, zone ->
                ZoneTarifaireFormCard(
                    zoneIndex = index,
                    zone = zone,
                    enabled = !uiState.isSubmitting,
                    readOnly = readOnly,
                    isLastField = index == uiState.zonesTarifaires.lastIndex,
                    onNomChange = { onZoneNomChange(index, it) },
                    onNbTablesChange = { onZoneNbTablesChange(index, it) },
                    onPrixDuM2Change = { onZonePrixDuM2Change(index, it) },
                    onSubmit = onSubmit
                )
            }
        }

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
            Text(if (readOnly) "Retour" else "Retour a la liste")
        }

        if (!readOnly) {
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
}

@Composable
private fun ZoneTarifaireFormCard(
    zoneIndex: Int,
    zone: ZoneTarifaireFormUiState,
    enabled: Boolean,
    readOnly: Boolean,
    isLastField: Boolean,
    onNomChange: (String) -> Unit,
    onNbTablesChange: (String) -> Unit,
    onPrixDuM2Change: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Zone tarifaire ${zoneIndex + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = zone.nom,
                onValueChange = onNomChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom de la zone") },
                singleLine = true,
                enabled = enabled && !readOnly,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = zone.nbTables,
                onValueChange = onNbTablesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de tables") },
                singleLine = true,
                enabled = enabled && !readOnly,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = zone.prixDuM2,
                onValueChange = onPrixDuM2Change,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Prix du m2") },
                singleLine = true,
                enabled = enabled && !readOnly,
                readOnly = readOnly,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (isLastField) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                )
            )
        }
    }
}
