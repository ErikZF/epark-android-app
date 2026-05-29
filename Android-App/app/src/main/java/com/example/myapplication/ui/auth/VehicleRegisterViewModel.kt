package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class VehicleRegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

class VehicleRegisterViewModel(
    private val repo: VehicleRepository = VehicleRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleRegisterUiState())
    val state: StateFlow<VehicleRegisterUiState> = _state.asStateFlow()

    fun addVehicle(plate: String, brand: String, model: String, year: String, onSuccess: () -> Unit) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearInt = year.trim().toIntOrNull()

        when {
            plate.isBlank() -> {
                _state.value = VehicleRegisterUiState(error = "La placa es requerida.")
                return
            }
            yearInt == null || yearInt < 1886 || yearInt > currentYear -> {
                _state.value = VehicleRegisterUiState(error = "Año inválido. Debe ser entre 1886 y $currentYear.")
                return
            }
        }

        _state.value = VehicleRegisterUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.addVehicle(
                    plate = plate.trim().uppercase(),
                    vehicleTypeId = 1.toShort(),
                    brand = brand.ifBlank { null },
                    model = model.ifBlank { null },
                )
                _state.value = VehicleRegisterUiState()
                onSuccess()
            } catch (e: Exception) {
                _state.value = VehicleRegisterUiState(error = "No se pudo registrar el vehículo.")
            }
        }
    }
}
