package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Fine
import com.example.myapplication.data.HistoryCache
import com.example.myapplication.data.NotificationStore
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.ProfileCache
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.data.repository.FineRepository
import com.example.myapplication.data.repository.PaymentRepository
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
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val sessionsCount: Int = 0,
    val paidSessionsCount: Int = 0,
    val finesCount: Int = 0,
    val notificationsCount: Int = 0,
    // True when one or more sections fell back to locally-cached data (offline).
    val isOffline: Boolean = false,
    val error: String? = null,
) {
    val vehiclesCount: Int get() = vehicles.size
    val paymentMethodsCount: Int get() = paymentMethods.size
}

class ProfileViewModel(
    private val vehicleRepo: VehicleRepository = VehicleRepository(),
    private val sessionRepo: SessionRepository = SessionRepository(),
    private val fineRepo: FineRepository = FineRepository(),
    private val paymentRepo: PaymentRepository = PaymentRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun refresh() {
        if (AuthState.userId <= 0) return

        // Show locally-cached data instantly so the profile is never blank while the network
        // request runs (offline-first, mirroring the history screen for requirement 13).
        val cachedSessions = HistoryCache.loadSessions()
        val cachedFines = HistoryCache.loadFines()
        _state.value = ProfileUiState(
            loading = true,
            fullName = AuthState.fullName,
            email = AuthState.email,
            initials = initialsOf(AuthState.fullName),
            vehicles = ProfileCache.loadVehicles(),
            paymentMethods = ProfileCache.loadPaymentMethods(),
            sessionsCount = cachedSessions.size,
            paidSessionsCount = cachedSessions.count(::isPaidSession),
            finesCount = cachedFines.size,
            notificationsCount = NotificationStore.all().size,
            isOffline = false,
        )

        viewModelScope.launch {
            var offline = false

            // Each section falls back to its local cache when the network fails, so the
            // profile summary stays usable offline (extends requirement 13's local history).
            val vehicles = runCatching { vehicleRepo.getVehicles() }
                .onSuccess { ProfileCache.saveVehicles(it) }
                .getOrElse { offline = true; ProfileCache.loadVehicles() }

            val paymentMethods = runCatching { paymentRepo.getMethods() }
                .onSuccess { ProfileCache.savePaymentMethods(it) }
                .getOrElse { offline = true; ProfileCache.loadPaymentMethods() }

            val sessions: List<ParkingSession> = runCatching { sessionRepo.getHistory() }
                .onSuccess { HistoryCache.saveSessions(it) }
                .getOrElse { offline = true; HistoryCache.loadSessions() }

            val fines: List<Fine> = runCatching { fineRepo.getUserFines() }
                .onSuccess { HistoryCache.saveFines(it) }
                .getOrElse { offline = true; HistoryCache.loadFines() }

            _state.value = ProfileUiState(
                loading = false,
                fullName = AuthState.fullName,
                email = AuthState.email,
                initials = initialsOf(AuthState.fullName),
                vehicles = vehicles,
                paymentMethods = paymentMethods,
                sessionsCount = sessions.size,
                paidSessionsCount = sessions.count(::isPaidSession),
                finesCount = fines.size,
                notificationsCount = NotificationStore.all().size,
                isOffline = offline,
            )
        }
    }

    fun clear() {
        _state.value = ProfileUiState(loading = false)
    }

    private fun initialsOf(name: String): String =
        name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }

    // A session counts as "paid" once it is no longer active (finalized/completed).
    private fun isPaidSession(session: ParkingSession): Boolean {
        val s = session.status.lowercase()
        return s != "activa" && s != "active"
    }
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
