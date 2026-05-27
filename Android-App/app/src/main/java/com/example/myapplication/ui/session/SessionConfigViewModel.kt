package com.example.myapplication.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionConfigUiState(
    val loading: Boolean = true,
    val currentVehicle: Vehicle? = null,
    val error: String? = null,
)

class SessionConfigViewModel(
    private val vehicleRepo: VehicleRepository = VehicleRepository(),
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
}
