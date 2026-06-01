package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AlertPreferences
import com.example.myapplication.data.NotificationHelper
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.payment.PaymentMethodsViewModel
import com.example.myapplication.ui.payment.SessionPaymentViewModel
import com.example.myapplication.ui.session.ActiveSessionViewModel
import com.example.myapplication.ui.session.ActiveSessionViewModel.Companion.formatElapsed
import com.example.myapplication.ui.session.SessionConfigViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun SessionConfigScreen(
    zone: ParkingZone,
    onStartParking: () -> Unit,
    onSelectOtherVehicle: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: SessionConfigViewModel = viewModel(),
) {
    var space by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()
    val currentVehicle = uiState.currentVehicle

    Scaffold(
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Text("Iniciar parqueo", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Ingresa el número del espacio físico donde estacionaste",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
            )
            Spacer(Modifier.height(16.dp))

            Text("Zona seleccionada", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MintAccent),
                        contentAlignment = Alignment.Center,
                    ) { Text("P", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(zone.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Horario:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(zone.hours, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(zone.rate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Número de espacio (máx. 4 dígitos)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = space,
                onValueChange = { input ->
                    if (input.length <= 4 && input.all { it.isDigit() }) space = input
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                cursorBrush = SolidColor(PrimaryGreen),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { _ ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(4) { i ->
                            val focused = i == space.length.coerceAtMost(3)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceWhite)
                                    .border(
                                        1.5.dp,
                                        if (focused) PrimaryGreen else BorderColor,
                                        RoundedCornerShape(12.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    space.getOrNull(i)?.toString() ?: "",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                },
            )
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vehículo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(currentVehicle?.plate ?: "Sin vehículo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(12.dp))
                        Text("${currentVehicle?.brand ?: ""} ${currentVehicle?.model ?: ""}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelectOtherVehicle),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Otro", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
            uiState.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (uiState.submitting) "Iniciando..." else "Iniciar parqueo",
                onClick = { vm.startSession(zone, space, onStartParking) },
                enabled = !uiState.submitting,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentScreen(
    sessionId: Int,
    totalCost: Double,
    duration: String,
    onConfirm: (invoiceNumber: String?) -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    methodsVm: PaymentMethodsViewModel = viewModel(),
    paymentVm: SessionPaymentViewModel = viewModel(),
) {
    val methodsState by methodsVm.state.collectAsState()
    val payState by paymentVm.state.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(methodsState.methods) {
        if (selectedId == null) selectedId = methodsState.methods.firstOrNull()?.id
    }

    val totalLabel = "₡${totalCost.toLong()}"

    Scaffold(
        topBar = { EparkTopBar("Pago de sesión", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            PaymentSummaryCard(
                total = totalLabel,
                spaceNumber = "",
                duration = duration,
            )
            Text("Método de pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            methodsState.methods.forEach { method ->
                PaymentMethodCard(
                    method = method,
                    selected = method.id == selectedId,
                    onSelect = { selectedId = method.id },
                )
            }
            if (payState.error != null) {
                ErrorBanner(payState.error!!)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (payState.loading) "Procesando..." else "Confirmar pago",
                enabled = !payState.loading,
                onClick = {
                    paymentVm.pay(
                        sessionId = sessionId,
                        amount = totalCost,
                        paymentMethodId = selectedId?.toIntOrNull(),
                        onSuccess = { invoice -> onConfirm(invoice) },
                    )
                },
            )
            SecondaryButton(text = "Regresar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    totalCost: Double,
    duration: String,
    zoneName: String,
    invoiceNumber: String? = null,
    onNewSession: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            val rows = buildList {
                add("Zona" to zoneName)
                add("Duración" to duration)
                add("Total pagado" to "₡${totalCost.toLong()}")
                if (invoiceNumber != null) add("Comprobante" to invoiceNumber)
            }
            SuccessReceipt(
                title = "¡Pago exitoso!",
                subtitle = "Tu comprobante está disponible en el historial",
                rows = rows,
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Nueva sesión", onClick = onNewSession)
        }
    }
}

@Composable
fun ActiveSessionScreen(
    onFinalized: (sessionId: Int, totalCost: Double, duration: String) -> Unit,
    onExtend: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: ActiveSessionViewModel,
) {
    val uiState by vm.state.collectAsState()
    val session = uiState.session
    val context = LocalContext.current

    // Disparar notificación local cuando la sesión está por vencer (solo una vez por transición)
    var notificationSent by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.nearingEnd) {
        if (uiState.nearingEnd && !notificationSent) {
            notificationSent = true
            NotificationHelper.showSessionExpiry(context, AlertPreferences.alertMinutes)
        } else if (!uiState.nearingEnd) {
            notificationSent = false
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        when {
            uiState.loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            uiState.error != null && session == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton(text = "Reintentar", onClick = { vm.loadActiveSession() }, modifier = Modifier.width(180.dp))
                    }
                }
            }
            session != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Sesión activa", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(session.zoneName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (uiState.nearingEnd) PendingRed else PrimaryGreen)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                formatElapsed(uiState.remainingSeconds),
                                color = SurfaceWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("TIEMPO RESTANTE", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                formatElapsed(uiState.remainingSeconds),
                                color = if (uiState.nearingEnd) PendingRed else PrimaryGreen,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Espacio #%04d · %s".format(session.spaceNumber, session.plate),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Text("Costo actual", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "₡${uiState.currentCostColones}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Text("Tarifa", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "₡${session.hourlyRate.toLong()}/hr",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (uiState.nearingEnd) {
                        WarningBanner("Quedan menos de 10 minutos para que cierre la zona")
                        Spacer(Modifier.height(8.dp))
                    }
                    uiState.error?.let {
                        ErrorBanner(it)
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    PrimaryButton(
                        text = if (uiState.finalizing) "Finalizando..." else "Finalizar y pagar",
                        onClick = {
                            vm.finalize { sessionId, cost, dur ->
                                onFinalized(sessionId, cost, dur)
                            }
                        },
                        enabled = !uiState.finalizing && !uiState.extending,
                    )
                    SecondaryButton(
                        text = "Extender sesión",
                        onClick = onExtend,
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ExtendSessionScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: ActiveSessionViewModel,
) {
    val uiState by vm.state.collectAsState()
    val session = uiState.session
    var hoursText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val maxMinutes = remember(session) { vm.maxExtensionMinutes() }
    val maxHours = maxMinutes / 60

    Scaffold(
        topBar = { EparkTopBar("Extender Sesión", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                session?.zoneName ?: "",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "¿Cuántas horas deseas agregar?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(4.dp))
                    if (maxHours > 0) {
                        Text(
                            "Máximo disponible: $maxHours ${if (maxHours == 1) "hora" else "horas"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = {
                            hoursText = it
                            localError = null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderColor,
                        ),
                        modifier = Modifier.width(120.dp),
                        placeholder = { Text("0", color = TextMuted) },
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "La zona cierra a las ${session?.zoneCloseHour ?: 22}:00 UTC. " +
                            "Si la extensión excede ese límite, se ajustará automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            localError?.let {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(it)
            }
            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(it)
            }
            Spacer(Modifier.height(12.dp))
            if (uiState.nearingEnd) {
                WarningBanner("Quedan menos de 10 minutos para que cierre la zona")
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = if (uiState.extending) "Extendiendo..." else "Aceptar",
                enabled = !uiState.extending,
                onClick = {
                    val hours = hoursText.trim().toIntOrNull()
                    when {
                        hours == null || hours <= 0 -> localError = "Ingresa un número válido de horas."
                        maxHours > 0 && hours > maxHours -> localError = "El máximo disponible es $maxHours ${if (maxHours == 1) "hora" else "horas"}."
                        else -> vm.extend(hours * 60, onSuccess = onBack, onError = { localError = it })
                    }
                },
            )
            SecondaryButton(text = "Regresar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}
