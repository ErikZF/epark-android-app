package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.auth.LoginViewModel
import com.example.myapplication.ui.auth.RegisterViewModel
import com.example.myapplication.ui.auth.VehicleRegisterViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun LoginScreen(
    onLoggedIn: (role: String) -> Unit,
    onRegister: () -> Unit,
    vm: LoginViewModel = viewModel(),
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
        BrandHeader()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            BrandTitle()
            Spacer(Modifier.height(24.dp))
            AuthTabs(
                selectedLogin = true,
                onLoginClick = {},
                onRegisterClick = onRegister,
            )
            Spacer(Modifier.height(20.dp))
            EparkTextField(value = email, onValueChange = { email = it }, placeholder = "Correo electrónico")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = password, onValueChange = { password = it }, placeholder = "Contraseña", isPassword = true)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextLink("¿Olvidaste tu contraseña?", onClick = {})
            }
            Spacer(Modifier.height(20.dp))
            uiState.error?.let { msg ->
                Text(msg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            if (uiState.needsVerification) {
                TextLink("Reenviar correo de verificación", onClick = { vm.resendVerification() })
                Spacer(Modifier.height(8.dp))
            }
            uiState.resendMessage?.let { msg ->
                Text(msg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = PrimaryGreen)
                Spacer(Modifier.height(8.dp))
            }
            PrimaryButton(
                text = if (uiState.loading) "Ingresando..." else "Continuar",
                onClick = { vm.login(email, password, onLoggedIn) },
                enabled = !uiState.loading,
            )
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("o continúa con", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SocialButton("G", Modifier.weight(1f))
                SocialButton("f", Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SocialButton(label: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.OutlinedButton(
        onClick = {},
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        modifier = modifier.height(48.dp),
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    vm: RegisterViewModel = viewModel(),
) {
    var name by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()

    // Navigate as soon as the polling coroutine marks the email as verified.
    LaunchedEffect(uiState.verified) {
        if (uiState.verified) onRegisterSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrandHeader()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                BrandTitle()
                Spacer(Modifier.height(24.dp))
                AuthTabs(
                    selectedLogin = false,
                    onLoginClick = onLoginClick,
                    onRegisterClick = {},
                )
                Spacer(Modifier.height(20.dp))
                EparkTextField(value = name, onValueChange = { name = it }, placeholder = "Nombre completo")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = cedula, onValueChange = { cedula = it }, placeholder = "Número de cédula")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = email, onValueChange = { email = it }, placeholder = "Correo electrónico")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = password, onValueChange = { password = it }, placeholder = "Contraseña", isPassword = true)
                Spacer(Modifier.height(20.dp))
                uiState.error?.let { msg ->
                    Text(msg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                PrimaryButton(
                    text = if (uiState.loading) "Creando cuenta..." else "Crear cuenta",
                    onClick = { vm.register(name, cedula, email, password) },
                    enabled = !uiState.loading && !uiState.awaitingVerification,
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        if (uiState.awaitingVerification) {
            VerificationWaitingOverlay(
                email = uiState.pendingEmail,
                resendMessage = uiState.resendMessage,
                onResend = { vm.resendVerification() },
            )
        }
    }
}

@Composable
private fun VerificationWaitingOverlay(
    email: String,
    resendMessage: String?,
    onResend: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0x99000000))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("📧", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Verifica tu correo",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hemos enviado un enlace de activación a\n$email",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(color = PrimaryGreen)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esperando verificación...",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.height(20.dp))
                resendMessage?.let { msg ->
                    Text(
                        msg,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = PrimaryGreen,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                TextLink("Reenviar correo de verificación", onClick = onResend)
            }
        }
    }
}

@Composable
fun VehicleRegisterScreen(
    onRegistered: () -> Unit,
    onExit: () -> Unit,
    vm: VehicleRegisterViewModel = viewModel(),
) {
    var plate by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    val uiState by vm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrandHeader()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                BrandTitle()
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Registre al menos 1 carro para continuar",
                    onClick = {},
                    enabled = false,
                )
                Spacer(Modifier.height(16.dp))
                EparkTextField(value = plate, onValueChange = { plate = it }, placeholder = "Placa del Vehículo")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = brand, onValueChange = { brand = it }, placeholder = "Marca")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = model, onValueChange = { model = it }, placeholder = "Modelo")
                Spacer(Modifier.height(12.dp))
                EparkTextField(value = year, onValueChange = { year = it }, placeholder = "Año")
                Spacer(Modifier.height(20.dp))
                uiState.error?.let { msg ->
                    Text(msg, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                PrimaryButton(
                    text = if (uiState.loading) "Registrando..." else "Registrar",
                    onClick = { vm.addVehicle(plate, brand, model, year) { showSuccess = true } },
                    enabled = !uiState.loading,
                )
                SecondaryButton(text = "Salir", onClick = onExit)
                Spacer(Modifier.height(24.dp))
            }
        }
        if (showSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                SuccessDialog(
                    title = "Éxito",
                    message = "¿Desea registrar más vehículos?",
                    primaryLabel = "Sí",
                    onPrimary = { showSuccess = false },
                    secondaryLabel = "No",
                    onSecondary = { onRegistered() },
                )
            }
        }
    }
}

@Composable
fun ExitLauncherScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LogoBadge()
        Spacer(Modifier.height(16.dp))
        Text("e-park", style = androidx.compose.material3.MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        PrimaryButton(text = "Volver al inicio", onClick = onBack)
    }
}
