package com.example.myapplication.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.repository.SessionRepository
import com.example.myapplication.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SessionConfigUiState(
    val loading: Boolean = true,
    val currentVehicle: Vehicle? = null,
    val submitting: Boolean = false,
    val error: String? = null,
)

class SessionConfigViewModel(
    private val vehicleRepo: VehicleRepository = VehicleRepository(),
    private val sessionRepo: SessionRepository = SessionRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(SessionConfigUiState())
    val state: StateFlow<SessionConfigUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = SessionConfigUiState(loading = false, currentVehicle = vehicleRepo.getVehicles().firstOrNull())
            } catch (e: Exception) {
                _state.value = SessionConfigUiState(loading = false, error = "No se pudo cargar el vehículo.")
            }
        }
    }

    /**
     * Validates the entered space and creates an active session for [zone].
     * Invokes [onSuccess] only when the backend accepts the session.
     */
    fun startSession(zone: ParkingZone, spaceText: String, onSuccess: () -> Unit) {
        val vehicle = _state.value.currentVehicle
        val space = spaceText.trim().toIntOrNull()

        when {
            vehicle == null -> {
                _state.value = _state.value.copy(error = "Agrega un vehículo antes de iniciar el parqueo.")
                return
            }
            space == null -> {
                _state.value = _state.value.copy(error = "Ingresa el número de espacio.")
                return
            }
            space < 1 || (zone.totalSpots > 0 && space > zone.totalSpots) -> {
                _state.value = _state.value.copy(
                    error = "El espacio debe estar entre 1 y ${zone.totalSpots}.",
                )
                return
            }
        }

        val zoneId = zone.id.toIntOrNull()
        if (zoneId == null) {
            _state.value = _state.value.copy(error = "Zona inválida. Vuelve a seleccionarla.")
            return
        }

        _state.value = _state.value.copy(submitting = true, error = null)
        viewModelScope.launch {
            try {
                val now = Date()
                sessionRepo.startSession(
                    vehicleId = vehicle.id,
                    zoneId = zoneId,
                    spaceNumber = space,
                    startIso = isoUtc(now),
                    endIso = isoUtc(Date(now.time + DEFAULT_DURATION_MS)),
                )
                _state.value = _state.value.copy(submitting = false, error = null)
                onSuccess()
            } catch (e: HttpException) {
                val message = if (e.code() == 409)
                    "Ese espacio ya está ocupado. Elige otro."
                else
                    "No se pudo iniciar la sesión."
                _state.value = _state.value.copy(submitting = false, error = message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(submitting = false, error = "No se pudo iniciar la sesión.")
            }
        }
    }

    private fun isoUtc(date: Date): String = isoFormat.format(date)

    private companion object {
        const val DEFAULT_DURATION_MS = 60L * 60L * 1000L // 1 hour default block

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
