package com.example.myapplication.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.repository.PaymentRepository
import com.example.myapplication.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionPaymentUiState(
    val loading: Boolean = false,
    val invoiceNumber: String? = null,
    val error: String? = null,
)

class SessionPaymentViewModel(
    private val payRepo: PaymentRepository = PaymentRepository(),
    private val sessionRepo: SessionRepository = SessionRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(SessionPaymentUiState())
    val state: StateFlow<SessionPaymentUiState> = _state.asStateFlow()

    fun pay(
        sessionId: Int,
        paymentMethodId: Int?,
        onSuccess: (actualCost: Double, invoiceNumber: String?) -> Unit,
    ) {
        _state.value = SessionPaymentUiState(loading = true)
        viewModelScope.launch {
            try {
                // 1. Finalizar la sesión → obtiene el costo real calculado por el servidor
                val finalized = sessionRepo.finalize(sessionId)
                // 2. Procesar el pago con el costo real
                val payment = payRepo.pay(finalized.totalCost.toDouble(), "session", finalized.id, paymentMethodId)
                _state.value = SessionPaymentUiState(invoiceNumber = payment.invoiceNumber)
                onSuccess(finalized.totalCost.toDouble(), payment.invoiceNumber)
            } catch (e: Exception) {
                _state.value = SessionPaymentUiState(error = "No se pudo procesar el pago. Intenta de nuevo.")
            }
        }
    }
}

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
