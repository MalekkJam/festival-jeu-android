package com.example.festivaljeumobile.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole
import com.example.festivaljeumobile.viewModel.user.UserFormEvent
import com.example.festivaljeumobile.viewModel.user.UserFormUiState
import com.example.festivaljeumobile.viewModel.user.UserFormViewModel

@Composable
fun UserFormScreen(
    initialUser: User? = null,
    onBackClick: () -> Unit,
    viewModel: UserFormViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialUser?.id) {
        viewModel.setInitialUser(initialUser)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UserFormEvent.Saved -> onBackClick()
            }
        }
    }

    UserFormContent(
        uiState = uiState,
        onLoginChange = viewModel::onLoginChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPrenomChange = viewModel::onPrenomChange,
        onNomChange = viewModel::onNomChange,
        onRoleChange = viewModel::onRoleChange,
        onSubmit = viewModel::submit,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormContent(
    uiState: UserFormUiState,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPrenomChange: (String) -> Unit,
    onNomChange: (String) -> Unit,
    onRoleChange: (UserRole) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    // Les rôles sélectionnables (Admin ne peut pas créer d'autres Admins via cette interface)
    val selectableRoles = listOf(
        UserRole.Benevole,
        UserRole.Organisateur,
        UserRole.SuperOrganisateur,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (uiState.isEditMode) "Modifier l'utilisateur" else "Créer un utilisateur",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = uiState.prenom,
            onValueChange = onPrenomChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Prénom") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = uiState.nom,
            onValueChange = onNomChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nom") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = uiState.login,
            onValueChange = onLoginChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Login *") },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            isError = uiState.error != null && uiState.login.isBlank(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    if (uiState.isEditMode)
                        "Nouveau mot de passe (laisser vide pour ne pas changer)"
                    else
                        "Mot de passe *"
                )
            },
            singleLine = true,
            enabled = !uiState.isSubmitting,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )

        ExposedDropdownMenuBox(
            expanded = roleDropdownExpanded,
            onExpandedChange = { roleDropdownExpanded = it },
        ) {
            OutlinedTextField(
                value = uiState.role.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Rôle *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                enabled = !uiState.isSubmitting,
            )
            ExposedDropdownMenu(
                expanded = roleDropdownExpanded,
                onDismissRequest = { roleDropdownExpanded = false },
            ) {
                selectableRoles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.name) },
                        onClick = {
                            onRoleChange(role)
                            roleDropdownExpanded = false
                        },
                    )
                }
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting,
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 2.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(if (uiState.isEditMode) "Mettre à jour" else "Créer l'utilisateur")
            }
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting,
        ) {
            Text("Retour à la liste")
        }
    }
}