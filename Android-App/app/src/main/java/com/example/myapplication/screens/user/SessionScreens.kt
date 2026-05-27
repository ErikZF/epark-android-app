package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.FakeData
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun SessionConfigScreen(
    zone: ParkingZone,
    onStartParking: () -> Unit,
    onSelectOtherVehicle: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var spaceDigits by remember { mutableStateOf(listOf("", "", "", "")) }
    val currentVehicle = FakeData.vehicles.first()

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
            Text("Ingresa el número del espacio físico donde estacionaste",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
                        Text("Horario Disponible:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(zone.hours, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(zone.rate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Número de espacio (4 dígitos)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .border(1.5.dp, BorderColor, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(spaceDigits[i], style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                        Text(currentVehicle.plate, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(12.dp))
                        Text("${currentVehicle.brand} ${currentVehicle.model}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Iniciar parqueo", onClick = onStartParking)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentScreen(
    zone: ParkingZone,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var selectedMethod by remember { mutableStateOf(FakeData.paymentMethods.first()) }

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
            PaymentSummaryCard(total = "₡ 2.000", spaceNumber = "#0042", duration = "02:02")
            Text("Método de pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            FakeData.paymentMethods.forEach { method ->
                PaymentMethodCard(
                    method = method,
                    selected = method.id == selectedMethod.id,
                    onSelect = { selectedMethod = method },
                )
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(text = "Confirmar pago", onClick = onConfirm)
            SecondaryButton(text = "Regresar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PaymentSuccessScreen(
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
            SuccessReceipt(
                title = "¡Pago exitoso!",
                subtitle = "Tu comprobante está disponible en el historial",
                rows = listOf(
                    "Zona" to "Centro San José",
                    "Espacio" to "#0042",
                    "Duración" to "02:02",
                    "Total pagado" to "₡ 2.000",
                ),
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Nueva sesión", onClick = onNewSession)
        }
    }
}

@Composable
fun ActiveSessionScreen(
    onFinalize: () -> Unit,
    onExtend: () -> Unit,
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
        ) {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Sesión activa", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("Zona Centro", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryGreen)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("1 hora", color = SurfaceWhite, fontWeight = FontWeight.Bold)
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
                    Text("TIEMPO TRANSCURRIDO", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(Modifier.height(12.dp))
                    Text("03: 40", color = PrimaryGreen, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Espacio #0042 PLACA ABC-123", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
                        Text("₡ 123", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                        Text("₡ 1200/hr", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            WarningBanner("Se te notificará 10 min antes de vencer")
            Spacer(Modifier.weight(1f))
            PrimaryButton(text = "Finalizar y pagar", onClick = onFinalize)
            SecondaryButton(text = "Extender sesión", onClick = onExtend)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ExtendSessionScreen(
    onAccept: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var hours by remember { mutableStateOf("") }

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
            Text("Zona Centro", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Digite la cantidad de horas que desea extender",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderColor,
                        ),
                        modifier = Modifier.width(120.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Nota: La cantidad máxima por sesión es de 12 hr. Si desea extender más favor comunicar con los administradores\nContactar al : 000-3213-123",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            WarningBanner("Se te notificará 10 min antes de vencer")
            Spacer(Modifier.weight(1f))
            PrimaryButton(text = "Aceptar", onClick = onAccept)
            SecondaryButton(text = "Regresar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}
