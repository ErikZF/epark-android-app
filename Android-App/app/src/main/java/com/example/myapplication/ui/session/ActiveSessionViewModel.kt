package com.example.myapplication.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ActiveSession
import com.example.myapplication.data.AlertPreferences
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.data.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class ActiveSessionUiState(
    val loading: Boolean = true,
    val session: ActiveSession? = null,
    val elapsedSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val currentCostColones: Long = 0L,
    val nearingEnd: Boolean = false,
    val finalizing: Boolean = false,
    val extending: Boolean = false,
    val error: String? = null,
)

class ActiveSessionViewModel(
    private val repo: SessionRepository = SessionRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ActiveSessionUiState())
    val state: StateFlow<ActiveSessionUiState> = _state.asStateFlow()

    private var tickerJob: Job? = null

    // No cargamos en init — esperamos a que el usuario esté autenticado
    fun loadActiveSession() {
        if (AuthState.userId <= 0) {
            _state.value = ActiveSessionUiState(loading = false)
            return
        }
        tickerJob?.cancel()
        _state.value = ActiveSessionUiState(loading = true)
        viewModelScope.launch {
            try {
                val session = repo.getActiveSession()
                if (session == null) {
                    _state.value = ActiveSessionUiState(loading = false, error = "No hay sesión activa.")
                } else {
                    _state.value = ActiveSessionUiState(loading = false, session = session)
                    startTicker(session)
                }
            } catch (e: Exception) {
                _state.value = ActiveSessionUiState(loading = false, error = "No se pudo cargar la sesión.")
            }
        }
    }

    private fun startTicker(session: ActiveSession) {
        tickerJob = viewModelScope.launch {
            while (true) {
                val nowMs = System.currentTimeMillis()
                val elapsed = ((nowMs - session.scheduledStartMs) / 1000L).coerceAtLeast(0L)
                val scheduledSeconds = (session.scheduledEndMs - session.scheduledStartMs) / 1000L
                val remainingSeconds = (scheduledSeconds - elapsed).coerceAtLeast(0L)
                val costColones = (session.hourlyRate * scheduledSeconds / 3600.0).toLong()
                val alertSeconds = (AlertPreferences.alertMinutes * 60).toLong()
                val nearingEnd = remainingSeconds in 1..alertSeconds

                _state.value = _state.value.copy(
                    elapsedSeconds = elapsed,
                    remainingSeconds = remainingSeconds,
                    currentCostColones = costColones,
                    nearingEnd = nearingEnd,
                )
                delay(1_000)
            }
        }
    }

    /**
     * Extends the session by [addedMinutes] (clamped server-side to zone close time).
     * Refreshes session data from the API on success.
     */
    fun extend(addedMinutes: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val session = _state.value.session ?: return
        _state.value = _state.value.copy(extending = true, error = null)
        viewModelScope.launch {
            try {
                repo.extend(session.id, addedMinutes)
                loadActiveSession()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(extending = false, error = "No se pudo extender la sesión.")
                onError("No se pudo extender la sesión.")
            }
        }
    }

    /**
     * Navega a la pantalla de pago sin finalizar aún la sesión.
     * La sesión se finaliza cuando el conductor confirma el pago.
     */
    fun proceedToPayment(onSuccess: (sessionId: Int, estimatedCost: Double, duration: String) -> Unit) {
        val session = _state.value.session ?: return
        val scheduledSeconds = (session.scheduledEndMs - session.scheduledStartMs) / 1000L
        val scheduledCost = session.hourlyRate * scheduledSeconds / 3600.0
        onSuccess(session.id, scheduledCost, formatElapsed(scheduledSeconds))
    }

    /** Limpia el estado después del pago exitoso. */
    fun clearSession() {
        tickerJob?.cancel()
        _state.value = ActiveSessionUiState(loading = false)
    }

    /** Maximum minutes the user can add before the zone closes. */
    fun maxExtensionMinutes(): Int {
        val session = _state.value.session ?: return 0
        val remaining = ((session.scheduledEndMs - System.currentTimeMillis()) / 60_000L).toInt()
        val zoneCloseMs = zoneCloseMs(session.scheduledEndMs, session.zoneCloseHour)
        return ((zoneCloseMs - session.scheduledEndMs) / 60_000L).toInt().coerceAtLeast(0)
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }

    companion object {
        fun formatElapsed(seconds: Long): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return "%d:%02d:%02d".format(h, m, s)
        }

        /** Returns the UTC ms of [closeHour] (local Costa Rica time) on the local day of [referenceMs]. */
        fun zoneCloseMs(referenceMs: Long, closeHour: Int): Long {
            val tz = TimeZone.getTimeZone("America/Costa_Rica")
            val cal = Calendar.getInstance(tz)
            cal.timeInMillis = referenceMs
            cal.set(Calendar.HOUR_OF_DAY, closeHour)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
