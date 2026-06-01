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

data class AdminAddZoneUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class AdminAddZoneViewModel(
    private val repo: ZoneRepository = ZoneRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminAddZoneUiState())
    val state: StateFlow<AdminAddZoneUiState> = _state.asStateFlow()

    fun save(
        municipalityId: Int,
        name: String,
        description: String,
        spacesStr: String,
        rateStr: String,
        latStr: String,
        lonStr: String,
    ) {
        val spots = spacesStr.trim().toIntOrNull()
        val hrRate = rateStr.trim().toDoubleOrNull()
        val lat = latStr.trim().toDoubleOrNull()
        val lon = lonStr.trim().toDoubleOrNull()

        val error = when {
            name.isBlank() -> "El nombre es requerido."
            spots == null || spots <= 0 -> "Los espacios deben ser un número mayor a 0."
            hrRate == null || hrRate <= 0 -> "La tarifa debe ser un valor mayor a 0."
            lat == null || lat < -90.0 || lat > 90.0 -> "Latitud inválida (debe estar entre -90 y 90)."
            lon == null || lon < -180.0 || lon > 180.0 -> "Longitud inválida (debe estar entre -180 y 180)."
            else -> null
        }
        if (error != null) {
            _state.value = AdminAddZoneUiState(error = error)
            return
        }

        _state.value = AdminAddZoneUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.addZone(municipalityId, name, description.takeIf { it.isNotBlank() }, lat!!, lon!!, spots!!, hrRate!!)
                _state.value = AdminAddZoneUiState(success = true)
            } catch (e: Exception) {
                _state.value = AdminAddZoneUiState(error = "No se pudo crear la zona.")
            }
        }
    }
}

data class AdminManageZoneUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class AdminManageZoneViewModel(
    private val repo: ZoneRepository = ZoneRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminManageZoneUiState())
    val state: StateFlow<AdminManageZoneUiState> = _state.asStateFlow()

    fun save(
        id: String,
        name: String,
        spacesStr: String,
        rateStr: String,
        openHourStr: String,
        closeHourStr: String,
        isActive: Boolean,
    ) {
        val spots = spacesStr.trim().toIntOrNull()
        val rate = rateStr.trim().toDoubleOrNull()
        val openHour = openHourStr.trim().toIntOrNull()
        val closeHour = closeHourStr.trim().toIntOrNull()
        val zoneId = id.toIntOrNull()

        val error = when {
            name.isBlank() -> "El nombre es requerido."
            spots == null || spots <= 0 -> "Los espacios deben ser un número mayor a 0."
            rate == null || rate <= 0 -> "La tarifa debe ser un valor mayor a 0."
            openHour == null || openHour < 0 || openHour > 23 -> "Hora de apertura inválida (0-23)."
            closeHour == null || closeHour < 1 || closeHour > 24 -> "Hora de cierre inválida (1-24)."
            closeHour <= openHour -> "La hora de cierre debe ser mayor a la de apertura."
            zoneId == null -> "ID de zona inválido."
            else -> null
        }
        if (error != null) {
            _state.value = AdminManageZoneUiState(error = error)
            return
        }

        _state.value = AdminManageZoneUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.updateZone(zoneId!!, name.trim(), spots!!, rate!!, openHour!!, closeHour!!, isActive)
                _state.value = AdminManageZoneUiState(success = true)
            } catch (e: Exception) {
                _state.value = AdminManageZoneUiState(error = "No se pudo actualizar la zona.")
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
