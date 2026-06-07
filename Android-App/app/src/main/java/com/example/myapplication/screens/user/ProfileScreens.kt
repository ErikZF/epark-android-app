package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AlertPreferences
import com.example.myapplication.data.Fine
import com.example.myapplication.data.NotificationStore
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.ui.payment.AddPaymentMethodViewModel
import java.util.Calendar
import com.example.myapplication.ui.auth.VehicleRegisterViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.payment.FinePaymentViewModel
import com.example.myapplication.ui.payment.PaymentMethodsViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.VehiclesViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun ProfileScreen(
    onEdit: () -> Unit,
    onVehicles: () -> Unit,
    onPaymentMethods: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: ProfileViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()
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
                    AvatarBadge(uiState.initials.ifBlank { "?" }, size = 56)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uiState.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(uiState.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = TextSecondary)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox(uiState.sessionsCount.toString(), "Sesiones", Modifier.weight(1f))
                    StatBox(uiState.paidCount.toString(), "Pagado", Modifier.weight(1f))
                    StatBox(uiState.finesCount.toString(), "Multas", Modifier.weight(1f))
                }
            }
            item {
                var alertMinutes by remember { mutableStateOf(AlertPreferences.alertMinutes) }
                var showAlertDialog by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        MenuRow(
                            icon = Icons.Outlined.DirectionsCar,
                            label = "Mis vehículos",
                            onClick = onVehicles,
                        )
                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
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
                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Timer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Alerta de vencimiento", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { showAlertDialog = true }) {
                                Text("$alertMinutes min", color = PrimaryGreen, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (showAlertDialog) {
                    AlertDialog(
                        onDismissRequest = { showAlertDialog = false },
                        title = { Text("Alerta de vencimiento") },
                        text = {
                            Column {
                                Text("Avísame cuando queden:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Spacer(Modifier.height(12.dp))
                                listOf(5, 10, 15, 20).forEach { mins ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = alertMinutes == mins,
                                            onClick = {
                                                alertMinutes = mins
                                                AlertPreferences.alertMinutes = mins
                                                showAlertDialog = false
                                            },
                                        )
                                        Text("$mins minutos")
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showAlertDialog = false }) { Text("Cancelar") }
                        },
                    )
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
    var name by remember { mutableStateOf(AuthState.fullName) }
    var email by remember { mutableStateOf(AuthState.email) }
    val initials = remember {
        name.trim().split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
    }

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
            AvatarBadge(initials, size = 72)
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
fun VehiclesScreen(
    onBack: () -> Unit,
    onAddVehicle: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: VehiclesViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()
    val vehicles = remember { mutableStateListOf<Vehicle>() }
    LaunchedEffect(uiState.vehicles) {
        vehicles.clear()
        vehicles.addAll(uiState.vehicles)
    }

    Scaffold(
        topBar = { EparkTopBar("Mis vehículos", onBack = onBack) },
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
                Text(
                    "Administra los vehículos registrados en epark",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddVehicle,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                ) {
                    Text("Agregar vehículo", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
            }
            items(vehicles) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    onDelete = { vehicles.remove(vehicle) },
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun AddVehicleScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: VehicleRegisterViewModel = viewModel(),
) {
    var plate by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()

    Scaffold(
        topBar = { EparkTopBar("Agregar vehículo", onBack = onBack) },
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
            Spacer(Modifier.height(8.dp))
            Text("Llene los datos para agregar nuevo vehículo", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(24.dp))
            EparkTextField(value = plate, onValueChange = { plate = it }, placeholder = "Placa del Vehículo")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = brand, onValueChange = { brand = it }, placeholder = "Marca")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = model, onValueChange = { model = it }, placeholder = "Modelo")
            Spacer(Modifier.height(12.dp))
            EparkTextField(value = year, onValueChange = { year = it }, placeholder = "Año")
            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (uiState.loading) "Agregando..." else "Agregar",
                enabled = !uiState.loading,
                onClick = { vm.addVehicle(plate, brand, model, year, onSuccess = onAdded) },
            )
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
    vm: PaymentMethodsViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()
    val methods = remember { mutableStateListOf<com.example.myapplication.data.PaymentMethod>() }
    var defaultId by remember { mutableStateOf("") }
    LaunchedEffect(uiState.methods) {
        methods.clear()
        methods.addAll(uiState.methods)
        if (defaultId.isBlank()) defaultId = uiState.methods.firstOrNull { it.isDefault }?.id ?: ""
    }

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
fun AddPaymentScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AddPaymentMethodViewModel = viewModel(),
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf<Int?>(null) }
    var expiryYear by remember { mutableStateOf<Int?>(null) }
    var showExpiryPicker by remember { mutableStateOf(false) }
    val expiry = if (expiryMonth != null && expiryYear != null)
        "%02d/%02d".format(expiryMonth, expiryYear!! % 100)
    else ""
    var cvc by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()

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
                Box(modifier = Modifier.weight(1f)) {
                    EparkTextField(
                        value = expiry,
                        onValueChange = {},
                        placeholder = "MM/AA",
                        enabled = false,
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showExpiryPicker = true })
                }
                EparkTextField(value = cvc, onValueChange = { cvc = it }, placeholder = "CVC", modifier = Modifier.weight(1f))
            }
            if (showExpiryPicker) {
                MonthYearPickerDialog(
                    initialMonth = expiryMonth,
                    initialYear = expiryYear,
                    onDismiss = { showExpiryPicker = false },
                    onConfirm = { month, year ->
                        expiryMonth = month
                        expiryYear = year
                        showExpiryPicker = false
                    },
                )
            }
            EparkTextField(value = holder, onValueChange = { holder = it }, placeholder = "Nombre del titular")
            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (uiState.loading) "Guardando..." else "Guardar tarjeta",
                enabled = !uiState.loading,
                onClick = {
                    vm.save(cardNumber, expiry, holder, onSuccess = onSaved)
                },
            )
            SecondaryButton(text = "Cancelar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotificationsScreen(onBack: () -> Unit, bottomBar: @Composable () -> Unit) {
    // Notifications are read once from the local per-user store on screen entry.
    val notifications = remember { NotificationStore.all() }

    Scaffold(
        topBar = { EparkTopBar("Notificaciones", onBack = onBack) },
        bottomBar = bottomBar,
        containerColor = AppBackground,
    ) { padding ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text("No tienes notificaciones", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(notifications) { notif ->
                    NotificationCard(title = notif.title, body = notif.body, time = notif.time)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun PayFineScreen(
    fine: Fine,
    onConfirm: (invoiceNumber: String?) -> Unit,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    bottomBar: @Composable () -> Unit,
    methodsVm: PaymentMethodsViewModel = viewModel(),
    payVm: FinePaymentViewModel = viewModel(),
) {
    val methodsState by methodsVm.state.collectAsState()
    val payState by payVm.state.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(methodsState.methods) {
        if (selectedId == null) selectedId = methodsState.methods.firstOrNull()?.id
    }

    Scaffold(
        topBar = { EparkTopBar("Pago de multa", onBack = onBack) },
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
            FineSummaryCard(fine = fine)
            Text("Método de pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (methodsState.methods.isEmpty() && !methodsState.loading) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Text("No tienes ninguna tarjeta registrada", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        PrimaryButton(text = "+ Agregar tarjeta", onClick = onAddCard)
                    }
                }
            } else {
                methodsState.methods.forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        selected = method.id == selectedId,
                        onSelect = { selectedId = method.id },
                    )
                }
            }
            if (payState.error != null) {
                ErrorBanner(payState.error!!)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = if (payState.loading) "Procesando..." else "Confirmar pago",
                enabled = !payState.loading && methodsState.methods.isNotEmpty(),
                onClick = {
                    payVm.pay(
                        fineId = fine.id.toInt(),
                        amount = fine.amountRaw,
                        paymentMethodId = selectedId?.toIntOrNull(),
                        onSuccess = { invoice -> onConfirm(invoice) },
                    )
                },
            )
            if (!payState.loading) {
                SecondaryButton(text = "Regresar", onClick = onBack)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearPickerDialog(
    initialMonth: Int?,
    initialYear: Int?,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit,
) {
    val cal = Calendar.getInstance()
    val nowMonth = cal.get(Calendar.MONTH) + 1
    val nowYear = cal.get(Calendar.YEAR)

    var pickedYear by remember { mutableStateOf(initialYear ?: nowYear) }
    var pickedMonth by remember { mutableStateOf(initialMonth ?: nowMonth) }

    val years = remember { (nowYear..nowYear + 20).toList() }
    val months = remember(pickedYear) {
        if (pickedYear == nowYear) (nowMonth..12).toList() else (1..12).toList()
    }

    LaunchedEffect(pickedYear) {
        if (pickedYear == nowYear && pickedMonth < nowMonth) pickedMonth = nowMonth
    }

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Fecha de vencimiento", fontWeight = FontWeight.SemiBold) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = "%02d".format(pickedMonth),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mes") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderColor,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false },
                    ) {
                        months.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(m)) },
                                onClick = { pickedMonth = m; monthExpanded = false },
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = pickedYear.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Año") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderColor,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false },
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = { pickedYear = y; yearExpanded = false },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pickedMonth, pickedYear) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
