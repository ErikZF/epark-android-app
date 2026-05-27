package com.example.myapplication.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentMethodsUiState(
    val loading: Boolean = true,
    val methods: List<PaymentMethod> = emptyList(),
    val error: String? = null,
)

class PaymentMethodsViewModel(
    private val repo: PaymentRepository = PaymentRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentMethodsUiState())
    val state: StateFlow<PaymentMethodsUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                _state.value = PaymentMethodsUiState(loading = false, methods = repo.getMethods())
            } catch (e: Exception) {
                _state.value = PaymentMethodsUiState(loading = false, error = "No se pudieron cargar las formas de pago.")
            }
        }
    }
}
