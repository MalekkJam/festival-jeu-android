package com.example.festivaljeumobile.viewModel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.model.UserRole
import com.example.festivaljeumobile.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserFormUiState(
    val userId: Long? = null,
    val login: String = "",
    val password: String = "",
    val prenom: String = "",
    val nom: String = "",
    val role: UserRole = UserRole.Benevole,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val isEditMode: Boolean
        get() = userId != null
}

sealed class UserFormEvent {
    object Saved : UserFormEvent()
}

class UserFormViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState: StateFlow<UserFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<UserFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setInitialUser(user: User?) {
        if (user == null) {
            _uiState.value = UserFormUiState()
            return
        }
        _uiState.value = UserFormUiState(
            userId = user.id?.toLong(),
            login = user.login,
            password = "",
            prenom = user.prenom ?: "",
            nom = user.nom ?: "",
            role = user.role,
        )
    }

    fun onLoginChange(value: String) {
        _uiState.update { it.copy(login = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onPrenomChange(value: String) {
        _uiState.update { it.copy(prenom = value, error = null) }
    }

    fun onNomChange(value: String) {
        _uiState.update { it.copy(nom = value, error = null) }
    }

    fun onRoleChange(role: UserRole) {
        _uiState.update { it.copy(role = role, error = null) }
    }

    fun submit() {
        val state = _uiState.value
        val login = state.login.trim()
        val password = state.password.trim()
        val prenom = state.prenom.trim().ifBlank { null }
        val nom = state.nom.trim().ifBlank { null }

        if (login.isBlank()) {
            _uiState.update { it.copy(error = "Le login est obligatoire.") }
            return
        }

        if (!state.isEditMode && password.isBlank()) {
            _uiState.update { it.copy(error = "Le mot de passe est obligatoire pour la création.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val result = if (state.isEditMode) {
                userRepository.updateUser(
                    id = state.userId!!,
                    login = login,
                    prenom = prenom,
                    nom = nom,
                    role = state.role,
                    password = password.ifBlank { null },
                )
            } else {
                userRepository.addUser(
                    login = login,
                    password = password,
                    prenom = prenom,
                    nom = nom,
                    role = state.role,
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.value = UserFormUiState()
                    _events.send(UserFormEvent.Saved)
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = throwable.message ?: "Impossible d'enregistrer l'utilisateur.",
                        )
                    }
                }
            )
        }
    }
}