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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Reservant
import com.example.festivaljeumobile.domain.model.ReservantType
import com.example.festivaljeumobile.viewModel.reservant.ReservantFormEvent
import com.example.festivaljeumobile.viewModel.reservant.ReservantFormViewModel
import kotlinx.coroutines.delay

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
                    snackbarHostState.showSnackbar("Reservant sauvegarde avec succes.")
                    delay(500)
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
                        text = when {
                            readOnly -> "Details du reservant"
                            uiState.isEditMode -> "Modifier un reservant"
                            else -> "Ajouter un reservant"
                        }
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
            if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

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
                ReservantTypeField(
                    value = uiState.type,
                    readOnly = readOnly,
                    enabled = !uiState.isSubmitting,
                    onValueChange = reservantFormViewModel::onTypeChange,
                )
            }

            item {
                if (readOnly) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retour a la liste")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
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
                                    modifier = Modifier.padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Text(
                                if (uiState.isEditMode) "Mettre a jour" else "Creer"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservantTypeField(
    value: ReservantType,
    readOnly: Boolean,
    enabled: Boolean,
    onValueChange: (ReservantType) -> Unit,
) {
    if (readOnly) {
        OutlinedTextField(
            value = value.name,
            onValueChange = {},
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value.name,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text("Type") },
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReservantType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        expanded = false
                        onValueChange(type)
                    }
                )
            }
        }
    }
}
