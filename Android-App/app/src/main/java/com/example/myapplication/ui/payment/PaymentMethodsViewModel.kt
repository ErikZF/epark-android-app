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

data class AddPaymentMethodUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)

class AddPaymentMethodViewModel(
    private val repo: PaymentRepository = PaymentRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AddPaymentMethodUiState())
    val state: StateFlow<AddPaymentMethodUiState> = _state.asStateFlow()

    fun save(
        cardNumber: String,
        expiry: String,
        holder: String,
        isDefault: Boolean = true,
        onSuccess: () -> Unit,
    ) {
        val digits = cardNumber.filter { it.isDigit() }
        val parts = expiry.replace(" ", "").split("/")
        val expMonth = parts.getOrNull(0)?.trim()?.toShortOrNull() ?: 0
        val expYear2d = parts.getOrNull(1)?.trim()?.toShortOrNull() ?: -1
        val expYear = if (expYear2d >= 0) (2000 + expYear2d).toShort() else (-1).toShort()

        val error = when {
            digits.length !in 13..19 -> "Número de tarjeta inválido."
            parts.size != 2 || expMonth !in 1..12 || expYear < 0 -> "Formato de vencimiento inválido (MM/AA)."
            holder.isBlank() -> "Ingresa el nombre del titular."
            else -> null
        }
        if (error != null) { _state.value = AddPaymentMethodUiState(error = error); return }

        val brand = when (digits.first()) {
            '4' -> "Visa"
            '5' -> "Mastercard"
            '3' -> "Amex"
            else -> null
        }
        val lastFour = digits.takeLast(4)

        _state.value = AddPaymentMethodUiState(loading = true)
        viewModelScope.launch {
            try {
                repo.addMethod(brand, lastFour, expMonth, expYear, isDefault)
                _state.value = AddPaymentMethodUiState(success = true)
                onSuccess()
            } catch (e: Exception) {
                _state.value = AddPaymentMethodUiState(error = "No se pudo guardar la tarjeta.")
            }
        }
    }
}

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
