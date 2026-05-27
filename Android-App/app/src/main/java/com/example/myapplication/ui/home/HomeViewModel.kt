package com.example.myapplication.ui.home

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
    val zones: List<ParkingZone> = emptyList(),
    val error: String? = null,
)

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
                _state.value = HomeUiState(zones = repo.getZones())
            } catch (e: Exception) {
                _state.value = HomeUiState(error = "No se pudieron cargar las zonas.")
            }
        }
    }
}
