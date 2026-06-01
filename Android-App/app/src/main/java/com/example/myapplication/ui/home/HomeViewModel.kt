package com.example.myapplication.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.repository.ZoneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val allZones: List<ParkingZone> = emptyList(),
    val municipalities: List<String> = emptyList(),
    val selectedMunicipality: String? = null,
    val userLat: Double? = null,
    val userLon: Double? = null,
    val error: String? = null,
) {
    val zones: List<ParkingZone>
        get() {
            val filtered = if (selectedMunicipality == null) allZones
                           else allZones.filter { it.municipalityName == selectedMunicipality }
            return if (userLat != null && userLon != null) {
                filtered.sortedBy { zone ->
                    val result = FloatArray(1)
                    Location.distanceBetween(userLat, userLon, zone.latitude, zone.longitude, result)
                    result[0]
                }
            } else filtered
        }

    fun distanceTo(zone: ParkingZone): Float? {
        if (userLat == null || userLon == null) return null
        val result = FloatArray(1)
        Location.distanceBetween(userLat, userLon, zone.latitude, zone.longitude, result)
        return result[0]
    }
}

class HomeViewModel(
    private val repo: ZoneRepository = ZoneRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(loading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val zones = repo.getZones()
                val municipalities = zones.map { it.municipalityName }.distinct().sorted()
                _state.value = _state.value.copy(
                    loading = false,
                    allZones = zones,
                    municipalities = municipalities,
                    selectedMunicipality = _state.value.selectedMunicipality
                        ?.takeIf { it in municipalities },
                    error = null,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "No se pudieron cargar las zonas.")
            }
        }
    }

    fun selectMunicipality(name: String?) {
        _state.value = _state.value.copy(selectedMunicipality = name)
    }

    fun updateLocation(lat: Double, lon: Double) {
        _state.value = _state.value.copy(userLat = lat, userLon = lon)
    }
}
