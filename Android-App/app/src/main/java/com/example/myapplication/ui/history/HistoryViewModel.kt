package com.example.myapplication.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Fine
import com.example.myapplication.data.HistoryCache
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.SessionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
            var sessions: List<ParkingSession> = emptyList()
            var fines: List<Fine> = emptyList()
            var offline = false
            var errorMsg: String? = null

            coroutineScope {
                val sessionsDeferred = async {
                    runCatching { sessionRepo.getHistory() }
                }
                val finesDeferred = async {
                    runCatching { fineRepo.getUserFines() }
                }

                val sessionsResult = sessionsDeferred.await()
                val finesResult = finesDeferred.await()

                sessionsResult.onSuccess { fetched ->
                    sessions = fetched
                    HistoryCache.saveSessions(fetched)
                }.onFailure {
                    val cached = HistoryCache.loadSessions()
                    if (cached.isNotEmpty()) {
                        sessions = cached
                        offline = true
                    } else {
                        errorMsg = "Sin conexión y sin datos guardados."
                    }
                }

                finesResult.onSuccess { fetched ->
                    fines = fetched
                }.onFailure {
                    errorMsg = errorMsg ?: "No se pudieron cargar las multas."
                }
            }

            _state.value = HistoryUiState(
                loading = false,
                sessions = sessions,
                fines = fines,
                error = errorMsg,
                isOffline = offline,
            )
        }
    }
}
