package com.example.festivaljeumobile.viewModel.festival

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.festivaljeumobile.FestivalApp
import com.example.festivaljeumobile.data.repository.OfflineException
import com.example.festivaljeumobile.domain.model.Festival
import com.example.festivaljeumobile.domain.repository.FestivalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FestivalListUiState(
    val festivals: List<Festival> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
)

class FestivalListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val festivalRepository: FestivalRepository
        get() = (getApplication<Application>() as FestivalApp).festivalRepository

    private val _uiState = MutableStateFlow(FestivalListUiState())
    val uiState: StateFlow<FestivalListUiState> = _uiState.asStateFlow()

    private var observeFestivalsJob: Job? = null

    init {
        loadFestivals()
    }

    fun loadFestivals() {
        if (observeFestivalsJob == null) {
            observeFestivalsJob = viewModelScope.launch(Dispatchers.IO) {
                festivalRepository.getAll().collect { festivals ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            festivals = festivals,
                            error = currentState.error?.takeIf { festivals.isEmpty() }
                        )
                    }
                }
            }
        }
        refreshFestivals()
    }

    fun refreshFestivals() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isOffline = false
                )
            }

            festivalRepository.refresh().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { currentState ->
                        val offlineError = throwable is OfflineException
                        currentState.copy(
                            isLoading = false,
                            isOffline = offlineError,
                            error = when {
                                offlineError && currentState.festivals.isNotEmpty() -> null
                                else -> throwable.message ?: "Impossible de recuperer les festivals."
                            }
                        )
                    }
                }
            )
        }
    }
}
