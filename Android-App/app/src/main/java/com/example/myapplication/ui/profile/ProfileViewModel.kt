package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.SessionRepository
import com.example.myapplication.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val fullName: String = "",
    val email: String = "",
    val initials: String = "",
    val vehicles: List<Vehicle> = emptyList(),
    val sessionsCount: Int = 0,
    val paidCount: Int = 0,
    val finesCount: Int = 0,
    val error: String? = null,
)

class ProfileViewModel(
    private val vehicleRepo: VehicleRepository = VehicleRepository(),
    private val sessionRepo: SessionRepository = SessionRepository(),
    private val fineRepo: FineRepository = FineRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun refresh() {
        if (AuthState.userId <= 0) return
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val vehicles = vehicleRepo.getVehicles()
                val sessions = sessionRepo.getHistory()
                val fines = fineRepo.getUserFines()
                _state.value = ProfileUiState(
                    loading = false,
                    fullName = AuthState.fullName,
                    email = AuthState.email,
                    initials = initialsOf(AuthState.fullName),
                    vehicles = vehicles,
                    sessionsCount = sessions.size,
                    paidCount = sessions.count { it.status == "Pagado" },
                    finesCount = fines.size,
                )
            } catch (e: Exception) {
                _state.value = ProfileUiState(
                    loading = false,
                    fullName = AuthState.fullName,
                    email = AuthState.email,
                    initials = initialsOf(AuthState.fullName),
                    error = "No se pudo cargar el perfil.",
                )
            }
        }
    }

    fun clear() {
        _state.value = ProfileUiState(loading = false)
    }

    private fun initialsOf(name: String): String =
        name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
}

data class VehiclesUiState(
    val loading: Boolean = true,
    val vehicles: List<Vehicle> = emptyList(),
    val error: String? = null,
)

class VehiclesViewModel(
    private val repo: VehicleRepository = VehicleRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(VehiclesUiState())
    val state: StateFlow<VehiclesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = VehiclesUiState(loading = false, vehicles = repo.getVehicles())
            } catch (e: Exception) {
                _state.value = VehiclesUiState(loading = false, error = "No se pudieron cargar los vehículos.")
            }
        }
    }
}
