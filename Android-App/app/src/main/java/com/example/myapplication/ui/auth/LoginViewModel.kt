package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    // True when login failed because the account email is not yet verified.
    val needsVerification: Boolean = false,
    val resendMessage: String? = null,
    val lastEmail: String = "",
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Attempts login; on success invokes [onSuccess] with the user's role. */
    fun login(email: String, password: String, onSuccess: (role: String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginUiState(error = "Ingresa correo y contraseña.")
            return
        }
        _state.value = LoginUiState(loading = true, lastEmail = email.trim())
        viewModelScope.launch {
            try {
                val role = repo.login(email, password)
                _state.value = LoginUiState()
                onSuccess(role)
            } catch (e: HttpException) {
                // 403 = account exists but email is not verified.
                if (e.code() == 403) {
                    val msg = parseMessage(e) ?: "Debes verificar tu correo electrónico antes de iniciar sesión."
                    _state.value = LoginUiState(error = msg, needsVerification = true, lastEmail = email.trim())
                } else {
                    _state.value = LoginUiState(error = "Correo o contraseña incorrectos.", lastEmail = email.trim())
                }
            } catch (e: Exception) {
                _state.value = LoginUiState(error = "No se pudo iniciar sesión. Verifica tu conexión.", lastEmail = email.trim())
            }
        }
    }

    /** Re-sends the verification email to the last attempted address. */
    fun resendVerification() {
        val email = _state.value.lastEmail
        if (email.isBlank()) return
        viewModelScope.launch {
            try {
                repo.resendVerification(email)
                _state.value = _state.value.copy(
                    resendMessage = "Correo de verificación reenviado. Revisa tu bandeja de entrada.",
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(resendMessage = "No se pudo reenviar el correo. Intenta de nuevo.")
            }
        }
    }

    private fun parseMessage(e: HttpException): String? = runCatching {
        e.response()?.errorBody()?.string()?.let { JSONObject(it).getString("message") }
    }.getOrNull()
}
