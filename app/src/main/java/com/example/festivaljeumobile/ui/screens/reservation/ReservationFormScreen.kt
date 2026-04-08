package com.example.festivaljeumobile.ui.screens.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.festivaljeumobile.domain.model.Reservation
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.model.Jeu
import com.example.festivaljeumobile.domain.model.ReservantOption
import com.example.festivaljeumobile.domain.model.ZoneTarifaire
import com.example.festivaljeumobile.viewModel.reservation.ReservationFormEvent
import com.example.festivaljeumobile.viewModel.reservation.ReservationFormUiState
import com.example.festivaljeumobile.viewModel.reservation.ReservationFormViewModel
import com.example.festivaljeumobile.viewModel.reservation.ReservationJeuFormUiState

private val etatDeSuiviOptions = listOf(
    "",
    "pas encore de contact",
    "contact pris",
    "discussion en cours",
    "refuse",
    "Present",
    "facture",
    "facture payée"
)

private val remiseOptions = listOf("", "Table", "Argent")

@Composable
fun ReservationFormScreen(
    initialReservation: Reservation? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReservationFormViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialReservation?.id) {
        viewModel.setInitialReservation(initialReservation)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ReservationFormEvent.Saved -> onBackClick()
            }
        }
    }

    ReservationFormContent(
        uiState = uiState,
        onReservantSelected = viewModel::onReservantSelected,
        onFestivalSelected = viewModel::onFestivalSelected,
        onEtatDeSuiviChange = viewModel::onEtatDeSuiviChange,
        onDateDeContactChange = viewModel::onDateDeContactChange,
        onRemiseChange = viewModel::onRemiseChange,
        onMontantRemiseChange = viewModel::onMontantRemiseChange,
        onFactureeChange = viewModel::onFactureeChange,
        onPayeeChange = viewModel::onPayeeChange,
        onAddJeuLine = viewModel::addJeuLine,
        onRemoveJeuLine = viewModel::removeJeuLine,
        onJeuSelected = viewModel::onJeuSelected,
        onZoneSelected = viewModel::onZoneSelected,
        onJeuNbTablesChange = viewModel::onJeuNbTablesChange,
        onJeuPlaceChange = viewModel::onJeuPlaceChange,
        onRetryLoadOptions = viewModel::loadOptions,
        onSubmit = viewModel::submit,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
private fun ReservationFormContent(
    uiState: ReservationFormUiState,
    onReservantSelected: (Int) -> Unit,
    onFestivalSelected: (Long) -> Unit,
    onEtatDeSuiviChange: (String) -> Unit,
    onDateDeContactChange: (String) -> Unit,
    onRemiseChange: (String) -> Unit,
    onMontantRemiseChange: (String) -> Unit,
    onFactureeChange: (Boolean) -> Unit,
    onPayeeChange: (Boolean) -> Unit,
    onAddJeuLine: () -> Unit,
    onRemoveJeuLine: (Int) -> Unit,
    onJeuSelected: (Int, Int) -> Unit,
    onZoneSelected: (Int, Int) -> Unit,
    onJeuNbTablesChange: (Int, String) -> Unit,
    onJeuPlaceChange: (Int, Boolean) -> Unit,
    onRetryLoadOptions: () -> Unit,
    onSubmit: () -> Unit,
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
            text = if (uiState.isEditMode) "Modification d'une reservation" else "Nouvelle reservation",
            style = MaterialTheme.typography.headlineSmall
        )

        if (uiState.isLoadingOptions) {
            CircularProgressIndicator()
        }

        ReservationDropdownField(
            label = "Nom du festival",
            options = uiState.festivals,
            selectedValue = uiState.festivals.firstOrNull { it.id == uiState.selectedFestivalId }?.nom
                ?: uiState.initialFestivalName,
            optionLabel = { it.nom },
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onOptionSelected = { onFestivalSelected(it.id) }
        )

        ReservationDropdownField(
            label = "Client",
            options = uiState.reservants,
            selectedValue = uiState.reservants.firstOrNull { it.id == uiState.selectedReservantId }?.nom
                ?: uiState.initialReservantName,
            optionLabel = { it.nom },
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onOptionSelected = { onReservantSelected(it.id) }
        )

        Button(
            onClick = onAddJeuLine,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(
                text = "Ajouter Jeu",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = "Allocation des Jeux",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (uiState.jeux.isEmpty()) {
            Text(
                text = "Aucun jeu ajoute",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            uiState.jeux.forEachIndexed { index, jeu ->
                ReservationJeuCard(
                    index = index,
                    jeu = jeu,
                    jeuxDisponibles = uiState.jeuxDisponibles,
                    zonesTarifaires = uiState.zonesTarifaires,
                    enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
                    onRemove = { onRemoveJeuLine(index) },
                    onJeuSelected = { onJeuSelected(index, it) },
                    onZoneSelected = { onZoneSelected(index, it) },
                    onNbTablesChange = { onJeuNbTablesChange(index, it) },
                    onPlaceChange = { onJeuPlaceChange(index, it) }
                )
            }
        }

        Text(
            text = "Suivi & Facturation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ReservationStringDropdownField(
            label = "Etat de suivi",
            options = etatDeSuiviOptions,
            selectedValue = uiState.etatDeSuivi,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onOptionSelected = onEtatDeSuiviChange,
            optionLabel = { if (it.isBlank()) "Aucun" else it }
        )

        OutlinedTextField(
            value = uiState.dateDeContact,
            onValueChange = onDateDeContactChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date de contact") },
            supportingText = { Text("Format : YYYY-MM-DD") },
            singleLine = true,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions
        )

        ReservationStringDropdownField(
            label = "Type de remise",
            options = remiseOptions,
            selectedValue = uiState.remise,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onOptionSelected = onRemiseChange,
            optionLabel = { if (it.isBlank()) "Aucune" else it }
        )

        OutlinedTextField(
            value = uiState.montantRemise,
            onValueChange = onMontantRemiseChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Montant de la remise") },
            singleLine = true,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        LabeledCheckbox(
            label = "Facturee",
            checked = uiState.facturee,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onCheckedChange = onFactureeChange
        )

        LabeledCheckbox(
            label = "Payee",
            checked = uiState.payee,
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions,
            onCheckedChange = onPayeeChange
        )

        if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onRetryLoadOptions,
                        enabled = !uiState.isSubmitting
                    ) {
                        Text("Recharger")
                    }
                }
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting && !uiState.isLoadingOptions
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(if (uiState.isEditMode) "Mettre a jour la reservation" else "Valider")
            }
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        ) {
            Text("Annuler")
        }
    }
}

@Composable
private fun ReservationJeuCard(
    index: Int,
    jeu: ReservationJeuFormUiState,
    jeuxDisponibles: List<Jeu>,
    zonesTarifaires: List<ZoneTarifaire>,
    enabled: Boolean,
    onRemove: () -> Unit,
    onJeuSelected: (Int) -> Unit,
    onZoneSelected: (Int) -> Unit,
    onNbTablesChange: (String) -> Unit,
    onPlaceChange: (Boolean) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jeu ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onRemove, enabled = enabled) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer le jeu"
                    )
                }
            }

            ReservationDropdownField(
                label = "Nom du jeu",
                options = jeuxDisponibles,
                selectedValue = jeuxDisponibles.firstOrNull { it.idJeu == jeu.jeuId }?.libelleJeu.orEmpty(),
                optionLabel = { it.libelleJeu },
                enabled = enabled,
                onOptionSelected = { onJeuSelected(it.idJeu) }
            )

            ReservationDropdownField(
                label = "Zone Tarifaire",
                options = zonesTarifaires,
                selectedValue = zonesTarifaires.firstOrNull { it.id == jeu.zoneTarifaireId }?.nom.orEmpty(),
                optionLabel = { it.nom },
                enabled = enabled,
                onOptionSelected = { zone -> zone.id?.let(onZoneSelected) }
            )

            OutlinedTextField(
                value = jeu.nbTables,
                onValueChange = onNbTablesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de tables") },
                singleLine = true,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            LabeledCheckbox(
                label = "Jeu place",
                checked = jeu.place,
                enabled = enabled,
                onCheckedChange = onPlaceChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ReservationDropdownField(
    label: String,
    options: List<T>,
    selectedValue: String,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onOptionSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationStringDropdownField(
    label: String,
    options: List<String>,
    selectedValue: String,
    enabled: Boolean,
    onOptionSelected: (String) -> Unit,
    optionLabel: (String) -> String,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = optionLabel(selectedValue),
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun LabeledCheckbox(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Text(text = label)
    }
}
