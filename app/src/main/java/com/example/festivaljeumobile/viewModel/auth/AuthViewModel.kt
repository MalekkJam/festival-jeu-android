package com.example.festivaljeumobile.viewModel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.data.service.AuthService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Success : AuthUiState()
}

sealed class AuthEvent {
    object NavigateToHome : AuthEvent()
    object NavigateToLogin: AuthEvent()
}

class AuthViewModel(
    private val authService: AuthService = AuthService.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _login = MutableStateFlow("")
    val login: StateFlow<String> = _login.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAuthChange(value: String) {
        _login.value = value
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        if (_uiState.value is AuthUiState.Error) _uiState.value = AuthUiState.Idle
    }

    fun onSubmit() {
        val login = _login.value.trim()
        val password = _password.value

        if (login.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Veuillez remplir tous les champs.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authService.login(login, password).fold(
                onSuccess = {

                    _login.value = ""
                    _password.value = ""
                    _events.send(AuthEvent.NavigateToHome)
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { throwable ->
                    Log.e("AuthViewModel", "Login error: ${throwable.message}")
                    _uiState.value = AuthUiState.Error(
                        throwable.message ?: "Identifiants incorrects."
                    )
                }
            )
        }
    }

    fun logout() {
        _uiState.value = AuthUiState.Idle
        _login.value = ""
        _password.value = ""
        viewModelScope.launch {
            _uiState.value = AuthUiState.Idle
            authRepository.logout().fold(
                onSuccess = {
                    _events.send(AuthEvent.NavigateToLogin)
                },
                onFailure = {
                    // même en cas d'erreur on déconnecte localement
                    _events.send(AuthEvent.NavigateToLogin)
                }
            )
        }
    }
 }