package com.example.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AdminReportSummary
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.ReportRepository
import com.example.myapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminZonesUiState(
    val loading: Boolean = true,
    val zones: List<ParkingZone> = emptyList(),
    val error: String? = null,
)

class AdminZonesViewModel(
    private val repo: ZoneRepository = ZoneRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminZonesUiState())
    val state: StateFlow<AdminZonesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = AdminZonesUiState(loading = false, zones = repo.getZones())
            } catch (e: Exception) {
                _state.value = AdminZonesUiState(loading = false, error = "No se pudieron cargar las zonas.")
            }
        }
    }
}

data class AdminReportsUiState(
    val loading: Boolean = true,
    val report: AdminReportSummary? = null,
    val error: String? = null,
)

class AdminReportsViewModel(
    private val repo: ReportRepository = ReportRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminReportsUiState())
    val state: StateFlow<AdminReportsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = AdminReportsUiState(loading = false, report = repo.summary())
            } catch (e: Exception) {
                _state.value = AdminReportsUiState(loading = false, error = "No se pudo cargar el reporte.")
            }
        }
    }
}

data class AdminFinesUiState(
    val loading: Boolean = true,
    val fines: List<Fine> = emptyList(),
    val error: String? = null,
)

class AdminFinesViewModel(
    private val repo: FineRepository = FineRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminFinesUiState())
    val state: StateFlow<AdminFinesUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = AdminFinesUiState(loading = false, fines = repo.getAllFines())
            } catch (e: Exception) {
                _state.value = AdminFinesUiState(loading = false, error = "No se pudieron cargar las multas.")
            }
        }
    }
}
