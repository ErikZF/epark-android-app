package com.example.myapplication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val loading: Boolean = true,
    val sessions: List<ParkingSession> = emptyList(),
    val fines: List<Fine> = emptyList(),
    val error: String? = null,
)

class HistoryViewModel(
    private val sessionRepo: SessionRepository = SessionRepository(),
    private val fineRepo: FineRepository = FineRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = HistoryUiState(
                    loading = false,
                    sessions = sessionRepo.getHistory(),
                    fines = fineRepo.getUserFines(),
                )
            } catch (e: Exception) {
                _state.value = HistoryUiState(loading = false, error = "No se pudo cargar la actividad.")
            }
        }
    }
}
