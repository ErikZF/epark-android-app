package com.example.myapplication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Fine
import com.example.myapplication.data.HistoryCache
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
    val isOffline: Boolean = false,
)

class HistoryViewModel(
    private val sessionRepo: SessionRepository = SessionRepository(),
    private val fineRepo: FineRepository = FineRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null, isOffline = false)
        viewModelScope.launch {
            try {
                val sessions = sessionRepo.getHistory()
                val fines = fineRepo.getUserFines()
                HistoryCache.saveSessions(sessions)
                _state.value = HistoryUiState(loading = false, sessions = sessions, fines = fines)
            } catch (e: Exception) {
                val cached = HistoryCache.loadSessions()
                if (cached.isNotEmpty()) {
                    _state.value = HistoryUiState(
                        loading = false,
                        sessions = cached,
                        isOffline = true,
                    )
                } else {
                    _state.value = HistoryUiState(loading = false, error = "Sin conexión y sin datos guardados.")
                }
            }
        }
    }
}
