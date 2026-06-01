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

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

class RegisterViewModel(
    private val repo: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun register(name: String, cedula: String, email: String, password: String, onSuccess: () -> Unit) {
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
                repo.register(fullName = name, email = email, password = password, nationalId = cedula.trim())
                _state.value = RegisterUiState()
                onSuccess()
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
