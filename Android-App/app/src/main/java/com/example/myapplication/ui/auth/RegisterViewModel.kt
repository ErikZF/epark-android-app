package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.NotificationStore
import com.example.myapplication.data.remote.AuthResponseDto
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val awaitingVerification: Boolean = false,
    val pendingEmail: String = "",
    val verified: Boolean = false,
    val resendMessage: String? = null,
)

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    // Held until email is verified, then committed to AuthState + DataStore.
    private var pendingAuth: AuthResponseDto? = null

    fun register(name: String, cedula: String, email: String, password: String) {
        if (name.isBlank() || cedula.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = RegisterUiState(error = "Completa todos los campos.")
            return
        }
        if (!EMAIL_REGEX.matches(email.trim())) {
            _state.value = RegisterUiState(error = "Correo electrónico inválido.")
            return
        }
        val passwordError = validatePassword(password)
        if (passwordError != null) {
            _state.value = RegisterUiState(error = passwordError)
            return
        }
        _state.value = RegisterUiState(loading = true)
        viewModelScope.launch {
            try {
                val auth = repo.register(fullName = name, email = email, password = password, nationalId = cedula.trim())
                pendingAuth = auth
                _state.value = RegisterUiState(awaitingVerification = true, pendingEmail = email.trim())
                startPolling(email.trim())
            } catch (e: HttpException) {
                val apiMessage = runCatching {
                    e.response()?.errorBody()?.string()?.let { JSONObject(it).getString("message") }
                }.getOrNull()
                _state.value = RegisterUiState(error = apiMessage ?: "No se pudo crear la cuenta. Intenta de nuevo.")
            } catch (e: Exception) {
                _state.value = RegisterUiState(error = "No se pudo crear la cuenta. Intenta de nuevo.")
            }
        }
    }

    private fun startPolling(email: String) {
        viewModelScope.launch {
            while (_state.value.awaitingVerification) {
                delay(5_000)
                try {
                    if (repo.checkVerification(email)) {
                        // Commit auth only now that the email is verified.
                        pendingAuth?.let {
                            repo.commitAuth(it)
                            // Persist the welcome notification so it stays in the
                            // Notifications screen (AuthState is populated by commitAuth).
                            NotificationStore.addWelcome(it.fullName)
                        }
                        _state.value = _state.value.copy(awaitingVerification = false, verified = true)
                        break
                    }
                } catch (_: Exception) { /* keep polling */ }
            }
        }
    }

    fun resendVerification() {
        val email = _state.value.pendingEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            try {
                repo.resendVerification(email)
                _state.value = _state.value.copy(resendMessage = "Correo reenviado. Revisa tu bandeja de entrada.")
            } catch (_: Exception) {
                _state.value = _state.value.copy(resendMessage = "No se pudo reenviar el correo. Intenta de nuevo.")
            }
        }
    }

    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    private fun validatePassword(password: String): String? {
        val missing = mutableListOf<String>()
        if (password.length < 8)                     missing.add("mínimo 8 caracteres")
        if (password.none { it.isUpperCase() })       missing.add("una mayúscula")
        if (password.none { it.isLowerCase() })       missing.add("una minúscula")
        if (password.none { it.isDigit() })           missing.add("un número")
        if (password.none { !it.isLetterOrDigit() })  missing.add("un carácter especial")
        return if (missing.isEmpty()) null
        else "La contraseña debe contener: ${missing.joinToString(", ")}."
    }
}
