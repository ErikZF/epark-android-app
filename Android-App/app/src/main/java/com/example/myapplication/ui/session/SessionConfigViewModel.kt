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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SessionConfigUiState(
    val loading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
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
                val vehicles = vehicleRepo.getVehicles()
                val current = _state.value.currentVehicle
                    ?.let { cur -> vehicles.find { it.id == cur.id } }
                    ?: vehicles.firstOrNull()
                _state.value = SessionConfigUiState(loading = false, vehicles = vehicles, currentVehicle = current)
            } catch (e: Exception) {
                _state.value = SessionConfigUiState(loading = false, error = "No se pudo cargar el vehículo.")
            }
        }
    }

    fun selectVehicle(vehicle: Vehicle) {
        _state.value = _state.value.copy(currentVehicle = vehicle)
    }

    /**
     * Validates the entered space and starts a session for [zone].
     * The session runs until the zone's closing time today (UTC).
     * Invokes [onSuccess] on the calling screen to navigate to the active session view.
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

        // Zone hours are in local (Costa Rica) time — compare with local time
        val nowLocal = Calendar.getInstance(TimeZone.getTimeZone("America/Costa_Rica"))
        val currentHour = nowLocal.get(Calendar.HOUR_OF_DAY)
        if (currentHour < zone.openHour || currentHour >= zone.closeHour) {
            _state.value = _state.value.copy(error = "La zona está cerrada en este horario.")
            return
        }

        _state.value = _state.value.copy(submitting = true, error = null)
        viewModelScope.launch {
            try {
                val now = Date()
                sessionRepo.startSession(
                    vehicleId = vehicle.id,
                    zoneId = zoneId,
                    spaceNumber = space!!,
                    startIso = isoUtc(now),
                )
                _state.value = _state.value.copy(submitting = false, error = null)
                onSuccess()
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    409 -> "Ese espacio ya está ocupado. Elige otro."
                    400 -> "La zona ya cerró por hoy o el espacio es inválido."
                    else -> "No se pudo iniciar la sesión."
                }
                _state.value = _state.value.copy(submitting = false, error = message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(submitting = false, error = "No se pudo iniciar la sesión.")
            }
        }
    }

    private fun isoUtc(date: Date): String = isoFormat.format(date)

    private companion object {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
