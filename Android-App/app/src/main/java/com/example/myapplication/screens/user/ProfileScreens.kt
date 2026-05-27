package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.FakeData
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun ProfileScreen(
    onEdit: () -> Unit,
    onAddVehicle: () -> Unit,
    onPaymentMethods: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarBadge(FakeData.userInitials, size = 56)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(FakeData.userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(FakeData.userEmail, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = TextSecondary)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(FakeData.userSessions.toString(), "Sesiones", Modifier.weight(1f))
                    StatBox(FakeData.userPaid.toString(), "Pagado", Modifier.weight(1f))
                    StatBox(FakeData.userFinesCount.toString(), "Multas", Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Mis vehículos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    TextLink("+ Agregar", onClick = onAddVehicle)
                }
            }
            items(FakeData.vehicles) { vehicle ->
                VehicleCard(vehicle = vehicle)
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        MenuRow(
                            icon = Icons.Outlined.CreditCard,
                            label = "Método de pago",
                            onClick = onPaymentMethods,
                        )
                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                        MenuRow(
                            icon = Icons.Outlined.Notifications,
                            label = "Notificaciones",
                            onClick = onNotifications,
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                DangerButton(text = "Cerrar Sesión", onClick = onLogout)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun EditProfileScreen(onBack: () -> Unit, bottomBar: @Composable () -> Unit) {
    var name by remember { mutableStateOf(FakeData.userName) }
    var email by remember { mutableStateOf(FakeData.userEmail) }

    Scaffold(
        topBar = { EparkTopBar("Editar perfil", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            AvatarBadge(FakeData.userInitials, size = 72)
            Spacer(Modifier.height(8.dp))
            TextLink("Editar foto de perfil", onClick = {})
            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Name", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                EparkTextField(value = name, onValueChange = { name = it }, placeholder = "Nombre")
            }
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Correo", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                EparkTextField(value = email, onValueChange = { email = it }, placeholder = "Correo")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddVehicleScreen(onBack: () -> Unit, onAdded: () -> Unit, bottomBar: @Composable () -> Unit) {
    var plate by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text("Agregar Vehículo", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Llene los datos para agregar nuevo vehículo", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(24.dp))
            EparkTextField(value = plate, onValueChange = { plate = it }, placeholder = "Placa del Vehículo")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = brand, onValueChange = { brand = it }, placeholder = "Marca")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = model, onValueChange = { model = it }, placeholder = "Modelo")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = year, onValueChange = { year = it }, placeholder = "Año")
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Agregar", onClick = onAdded)
            SecondaryButton(text = "Salir", onClick = onBack)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentMethodsScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val methods = remember { FakeData.paymentMethods.toMutableStateList() }
    var defaultId by remember { mutableStateOf(methods.firstOrNull { it.isDefault }?.id ?: "") }

    Scaffold(
        topBar = { EparkTopBar("Formas de pago", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Agregar, consultar y administrar las formas de pago guardadas en epark",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddCard,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                ) {
                    Text("Agregar forma de pago", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
            }
            items(methods) { method ->
                PaymentMethodCard(
                    method = method,
                    selected = method.id == defaultId,
                    onSelect = { defaultId = method.id },
                    onDelete = { methods.remove(method) },
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AddPaymentScreen(onBack: () -> Unit, onSaved: () -> Unit, bottomBar: @Composable () -> Unit) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }

    Scaffold(
        topBar = { EparkTopBar("Agregar una forma de pago", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            EparkTextField(value = cardNumber, onValueChange = { cardNumber = it }, placeholder = "Número de tarjeta")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EparkTextField(value = expiry, onValueChange = { expiry = it }, placeholder = "MM / AA", modifier = Modifier.weight(1f))
                EparkTextField(value = cvc, onValueChange = { cvc = it }, placeholder = "CVC", modifier = Modifier.weight(1f))
            }
            EparkTextField(value = holder, onValueChange = { holder = it }, placeholder = "Nombre del titular")
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                PrimaryButton(text = "Guardar", onClick = onSaved, modifier = Modifier.width(160.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit, bottomBar: @Composable () -> Unit) {
    Scaffold(
        topBar = { EparkTopBar("Notificaciones", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(FakeData.notifications) { notif ->
                NotificationCard(title = notif.title, body = notif.body, time = notif.time)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun PayFineScreen(
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var selectedMethod by remember { mutableStateOf(FakeData.paymentMethods.first()) }

    Scaffold(
        topBar = { EparkTopBar("Pago de multas", onBack = onBack) },
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
            PaymentSummaryCard(total = "₡ 8.200", spaceNumber = "#0086", duration = "06:02")
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
