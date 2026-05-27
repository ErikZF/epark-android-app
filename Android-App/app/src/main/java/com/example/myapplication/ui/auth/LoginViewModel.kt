package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
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
        _state.value = LoginUiState(loading = true)
        viewModelScope.launch {
            try {
                val role = repo.login(email, password)
                _state.value = LoginUiState()
                onSuccess(role)
            } catch (e: Exception) {
                _state.value = LoginUiState(error = "No se pudo iniciar sesión. Verifica tus datos.")
            }
        }
    }
}
