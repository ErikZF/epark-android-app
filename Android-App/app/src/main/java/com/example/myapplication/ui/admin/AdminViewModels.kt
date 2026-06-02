package com.example.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AdminActionLog
import com.example.myapplication.data.AdminReportSummary
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.AdminLogRepository
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.ReportRepository
import com.example.myapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val zones = repo.getZones(municipalityId = AuthState.municipalityId.takeIf { it > 0 }, includeInactive = true)
                _state.value = AdminZonesUiState(loading = false, zones = zones)
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
    val fromDate: String = "",
    val toDate: String = "",
)

class AdminReportsViewModel(
    private val repo: ReportRepository = ReportRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminReportsUiState())
    val state: StateFlow<AdminReportsUiState> = _state.asStateFlow()

    init { refresh() }

    fun setFromDate(date: String) {
        _state.value = _state.value.copy(fromDate = date)
    }

    fun setToDate(date: String) {
        _state.value = _state.value.copy(toDate = date)
    }

    fun refresh() {
        val from = _state.value.fromDate.takeIf { it.isNotBlank() }
        val to = _state.value.toDate.takeIf { it.isNotBlank() }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(loading = false, report = repo.summary(from, to, AuthState.municipalityId.takeIf { it > 0 }))
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "No se pudo cargar el reporte.")
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
        openHourStr: String,
        closeHourStr: String,
    ) {
        val spots = spacesStr.trim().toIntOrNull()
        val hrRate = rateStr.trim().toDoubleOrNull()
        val lat = latStr.trim().toDoubleOrNull()
        val lon = lonStr.trim().toDoubleOrNull()
        val openHour = openHourStr.trim().toIntOrNull()
        val closeHour = closeHourStr.trim().toIntOrNull()

        val error = when {
            name.isBlank() -> "El nombre es requerido."
            spots == null || spots <= 0 -> "Los espacios deben ser un número mayor a 0."
            hrRate == null || hrRate <= 0 -> "La tarifa debe ser un valor mayor a 0."
            lat == null || lat < -90.0 || lat > 90.0 -> "Latitud inválida (debe estar entre -90 y 90)."
            lon == null || lon < -180.0 || lon > 180.0 -> "Longitud inválida (debe estar entre -180 y 180)."
            openHour == null || openHour < 0 || openHour > 23 -> "Hora de apertura inválida (0-23)."
            closeHour == null || closeHour < 1 || closeHour > 24 -> "Hora de cierre inválida (1-24)."
            closeHour <= openHour -> "La hora de cierre debe ser mayor a la de apertura."
            else -> null
        }
        if (error != null) {
            _state.value = AdminAddZoneUiState(error = error)
            return
        }

        _state.value = AdminAddZoneUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.addZone(municipalityId, name, description.takeIf { it.isNotBlank() }, lat!!, lon!!, spots!!, hrRate!!, openHour!!, closeHour!!)
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
    val savedAt: String? = null,
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
                val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                _state.value = AdminManageZoneUiState(success = true, savedAt = timestamp)
            } catch (e: Exception) {
                _state.value = AdminManageZoneUiState(error = "No se pudo actualizar la zona.")
            }
        }
    }
}

data class AdminLogsUiState(
    val loading: Boolean = true,
    val logs: List<AdminActionLog> = emptyList(),
    val error: String? = null,
)

class AdminLogsViewModel(
    private val repo: AdminLogRepository = AdminLogRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminLogsUiState())
    val state: StateFlow<AdminLogsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = AdminLogsUiState(loading = false, logs = repo.getLogs())
            } catch (e: Exception) {
                _state.value = AdminLogsUiState(loading = false, error = "No se pudo cargar la bitácora.")
            }
        }
    }
}

data class AdminAlertsUiState(
    val loading: Boolean = true,
    val alerts: List<com.example.myapplication.data.AdminAlert> = emptyList(),
    val error: String? = null,
)

class AdminAlertsViewModel(
    private val repo: com.example.myapplication.data.repository.AdminNotificationRepository =
        com.example.myapplication.data.repository.AdminNotificationRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminAlertsUiState())
    val state: StateFlow<AdminAlertsUiState> = _state.asStateFlow()

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val alerts = repo.getNotifications(AuthState.municipalityId.takeIf { it > 0 })
                _state.value = AdminAlertsUiState(loading = false, alerts = alerts)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "No se pudieron cargar las alertas.")
            }
        }
    }
}

data class AdminIssueFineUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)

class AdminIssueFineViewModel(
    private val fineRepo: FineRepository = FineRepository(),
    private val zoneRepo: ZoneRepository = ZoneRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AdminIssueFineUiState())
    val state: StateFlow<AdminIssueFineUiState> = _state.asStateFlow()

    fun issue(plate: String, zoneId: String, spaceNumberStr: String, reason: String, amountStr: String, zoneTotalSpots: Int) {
        val amount = amountStr.trim().toDoubleOrNull()
        val zoneIdInt = zoneId.toIntOrNull()
        val spaceNumber = spaceNumberStr.trim().toIntOrNull()
        val error = when {
            plate.isBlank() -> "La placa es requerida."
            reason.isBlank() -> "El motivo es requerido."
            amount == null || amount <= 0 -> "El monto debe ser mayor a 0."
            zoneIdInt == null -> "Selecciona una zona."
            spaceNumber == null || spaceNumber <= 0 -> "El espacio debe ser un número mayor a 0."
            spaceNumber > zoneTotalSpots -> "Espacio inválido. La zona tiene solo $zoneTotalSpots espacios."
            else -> null
        }
        if (error != null) { _state.value = AdminIssueFineUiState(error = error); return }

        _state.value = AdminIssueFineUiState(loading = true)
        viewModelScope.launch {
            try {
                val vehicle = ApiClient.api.getVehicleByPlate(plate.trim().uppercase())
                fineRepo.issueFine(vehicle.id, zoneIdInt!!, spaceNumber!!, reason.trim(), amount!!)
                _state.value = AdminIssueFineUiState(success = true)
            } catch (e: retrofit2.HttpException) {
                val msg = if (e.code() == 404) "Placa no encontrada." else "Error al emitir multa."
                _state.value = AdminIssueFineUiState(error = msg)
            } catch (e: Exception) {
                _state.value = AdminIssueFineUiState(error = "Error al emitir multa.")
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
                _state.value = AdminFinesUiState(loading = false, fines = repo.getAllFines(AuthState.municipalityId.takeIf { it > 0 }))
            } catch (e: Exception) {
                _state.value = AdminFinesUiState(loading = false, error = "No se pudieron cargar las multas.")
            }
        }
    }
}
