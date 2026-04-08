package com.example.festivaljeumobile.viewModel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.domain.model.User
import com.example.festivaljeumobile.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserListUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val deletingUserId: Long? = null,
)

class UserListViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getAllUsers().fold(
                onSuccess = { users ->
                    _uiState.update { it.copy(users = users, isLoading = false) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Impossible de récupérer les utilisateurs.",
                        )
                    }
                }
            )
        }
    }

    fun deleteUser(user: User) {
        val userId = user.id?.toLong() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(deletingUserId = userId, error = null) }
            userRepository.deleteUser(userId).fold(
                onSuccess = {
                    _uiState.update { currentState ->
                        currentState.copy(
                            users = currentState.users.filter { it.id != user.id },
                            deletingUserId = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            deletingUserId = null,
                            error = throwable.message ?: "Impossible de supprimer l'utilisateur.",
                        )
                    }
                }
            )
        }
    }
}