package com.example.myapplication.screens.admin

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.StaticContent
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.ui.admin.AdminAddZoneViewModel
import com.example.myapplication.ui.admin.AdminAlertsViewModel
import com.example.myapplication.ui.admin.AdminFinesViewModel
import com.example.myapplication.ui.admin.AdminIssueFineViewModel
import com.example.myapplication.ui.admin.AdminLogsViewModel
import com.example.myapplication.ui.admin.AdminManageZoneViewModel
import com.example.myapplication.ui.admin.AdminReportsViewModel
import com.example.myapplication.ui.admin.AdminZonesViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

// ─────────────────── Admin Zones ───────────────────────────────────────────

@Composable
fun AdminZonesScreen(
    onManageZone: (ParkingZone) -> Unit,
    onAddZone: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminZonesViewModel = viewModel(),
) {
    var search by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()
    val zones = uiState.zones.filter {
        search.isBlank() || it.name.contains(search, ignoreCase = true)
    }

    Scaffold(bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("Municipalidad", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(StaticContent.municipality, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    StatusChip("Admin")
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!uiState.loading) TextLink("+ Agregar zona", onClick = onAddZone)
                }
            }
            items(zones) { zone ->
                AdminZoneCard(zone = zone, onManageClick = { onManageZone(zone) })
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────── Admin Add Zone ───────────────────────────────────────

@Composable
fun AdminAddZoneScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminAddZoneViewModel = viewModel(),
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var spaces by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf("6") }
    var closeHour by remember { mutableStateOf("22") }

    val uiState by vm.state.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onSaved()
    }

    Scaffold(topBar = { EparkTopBar("Agregar zona", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Municipalidad", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(StaticContent.municipality, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nueva zona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LabeledField("Nombre", name) { name = it }
                    LabeledField("Descripción (opcional)", description) { description = it }
                    LabeledField("Espacios totales", spaces) { spaces = it }
                    LabeledField("Tarifa/hr (₡)", rate) { rate = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Latitud", latitude, Modifier.weight(1f)) { latitude = it }
                        LabeledField("Longitud", longitude, Modifier.weight(1f)) { longitude = it }
                    }
                    Text("Horario (hora en formato 0-23)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Desde", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(openHour, { openHour = it }, "6", Modifier.fillMaxWidth())
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Hasta", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(closeHour, { closeHour = it }, "22", Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(uiState.error!!)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (uiState.loading) "Guardando..." else "Guardar zona",
                enabled = !uiState.loading,
                onClick = {
                    vm.save(AuthState.municipalityId, name.trim(), description.trim(), spaces, rate, latitude, longitude, openHour, closeHour)
                },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        EparkTextField(value = value, onValueChange = onValueChange, placeholder = label)
    }
}

// ─────────────────── Admin Manage Zone ─────────────────────────────────────

@Composable
fun AdminManageZoneScreen(
    zone: ParkingZone,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminManageZoneViewModel = viewModel(),
) {
    var name by remember { mutableStateOf(zone.name) }
    var spaces by remember { mutableStateOf(zone.totalSpots.toString()) }
    var rate by remember { mutableStateOf(zone.hourlyRate.toLong().toString()) }
    var openHour by remember { mutableStateOf(zone.openHour.toString()) }
    var closeHour by remember { mutableStateOf(zone.closeHour.toString()) }
    var isActive by remember { mutableStateOf(zone.isActive) }

    val uiState by vm.state.collectAsState()

    Scaffold(topBar = { EparkTopBar("Gestionar zona", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(MintAccent),
                        contentAlignment = Alignment.Center,
                    ) { Text("P", color = PrimaryGreen, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Horario Disponible:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(zone.hours, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Espacios: ${zone.totalSpots}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(zone.rate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    StatusChip(if (isActive) "Activa" else "Inactiva")
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField("Nombre", name) { name = it }
                    LabeledField("Espacios", spaces) { spaces = it }
                    LabeledField("Tarifa/hr (₡)", rate) { rate = it }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MintAccent)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                        Text(
                            "Los cambios de tarifa aplican solo a sesiones nuevas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen,
                        )
                    }
                    Text("Horario (hora en formato 0-23)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Desde", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(openHour, { openHour = it }, "6", Modifier.fillMaxWidth())
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Hasta", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(closeHour, { closeHour = it }, "22", Modifier.fillMaxWidth())
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Zona activa", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            }
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(uiState.error!!)
            }
            if (uiState.success && uiState.savedAt != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MintAccent)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Text("Cambios guardados el ${uiState.savedAt}", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen)
                }
            }
            Spacer(Modifier.height(24.dp))
            if (!uiState.success) {
                PrimaryButton(
                    text = if (uiState.loading) "Guardando..." else "Confirmar",
                    enabled = !uiState.loading,
                    onClick = { vm.save(zone.id, name, spaces, rate, openHour, closeHour, isActive) },
                )
            }
            SecondaryButton(
                text = if (uiState.success) "Volver" else "Cancelar",
                onClick = if (uiState.success) onConfirm else onBack,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────── Admin Reports ────────────────────────────────────────

@Composable
fun AdminReportsScreen(
    onViewLogs: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminReportsViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()
    val report = uiState.report
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    fun showDatePicker(current: String, onPicked: (String) -> Unit) {
        val parts = current.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, year, month, day ->
            onPicked("%04d-%02d-%02d".format(year, month + 1, day))
        }, y, m, d).show()
    }

    Scaffold(bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reportes", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    TextLink("Bitácora", onClick = onViewLogs)
                }
            }
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Filtrar por rango de fechas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { showDatePicker(uiState.fromDate) { vm.setFromDate(it) } },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (uiState.fromDate.isBlank()) "Desde" else uiState.fromDate, style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = { showDatePicker(uiState.toDate) { vm.setToDate(it) } },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (uiState.toDate.isBlank()) "Hasta" else uiState.toDate, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        PrimaryButton(
                            text = if (uiState.loading) "Cargando..." else "Buscar",
                            enabled = !uiState.loading,
                            onClick = { vm.refresh() },
                        )
                        if (uiState.fromDate.isNotBlank() || uiState.toDate.isNotBlank()) {
                            SecondaryButton("Limpiar filtros", onClick = {
                                vm.setFromDate("")
                                vm.setToDate("")
                                vm.refresh()
                            })
                        }
                    }
                }
            }
            if (uiState.error != null) {
                item { ErrorBanner(uiState.error!!) }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Default.LocalParking, "Total sesiones", (report?.totalSessions ?: 0).toString(), modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.CreditCard, "Ingresos", report?.revenue ?: "—", modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Default.Warning, "Multas emitidas", (report?.finesIssued ?: 0).toString(), iconTint = PendingRed, modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.Place, "Espacios activos", report?.activeSpots ?: "—", iconTint = WarningOrange, modifier = Modifier.weight(1f))
                }
            }
            val zones = report?.revenueByZone ?: emptyList()
            if (zones.isNotEmpty()) {
                item {
                    Text("Ingresos por zona", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(zones) { z ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(z.zoneName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${z.sessions} sesiones", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(z.revenue, style = MaterialTheme.typography.bodyMedium, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────── Admin Action Log ─────────────────────────────────────

@Composable
fun AdminLogsScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminLogsViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()

    Scaffold(topBar = { EparkTopBar("Bitácora de acciones", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Registro de todas las acciones realizadas por administradores.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            if (uiState.error != null) {
                item { ErrorBanner(uiState.error!!) }
            }
            if (!uiState.loading && uiState.error == null && uiState.logs.isEmpty()) {
                item {
                    Text(
                        "No hay acciones registradas aún.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                }
            }
            items(uiState.logs) { log ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(log.action)
                            Text("${log.date} ${log.time}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        if (log.details.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(log.details, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Text(log.adminName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────── Admin Fines ──────────────────────────────────────────

@Composable
fun AdminFinesScreen(
    onIssueFine: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminFinesViewModel = viewModel(),
) {
    val uiState by vm.state.collectAsState()
    val pendingCount = uiState.fines.count { !it.isPaid }

    Scaffold(bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Multas", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    TextLink("+ Emitir multa", onClick = onIssueFine)
                }
                Spacer(Modifier.height(8.dp))
                if (pendingCount > 0) ErrorBanner("$pendingCount multas pendientes de cobro")
            }
            items(uiState.fines) { fine ->
                FineRow(fine = fine)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────── Admin Issue Fine ─────────────────────────────────────

@Composable
fun AdminIssueFineScreen(
    zones: List<com.example.myapplication.data.ParkingZone>,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminIssueFineViewModel = viewModel(),
) {
    var plate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("5000") }
    var spaceNumber by remember { mutableStateOf("") }
    var selectedZoneId by remember { mutableStateOf("") }
    var zoneMenuExpanded by remember { mutableStateOf(false) }
    val selectedZoneName = zones.firstOrNull { it.id == selectedZoneId }?.name ?: "Seleccionar zona"

    val uiState by vm.state.collectAsState()
    LaunchedEffect(uiState.success) { if (uiState.success) onSaved() }

    Scaffold(topBar = { EparkTopBar("Emitir multa", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField("Placa del vehículo", plate) { plate = it.uppercase() }
                    Column {
                        Text("Zona", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Box {
                            OutlinedButton(onClick = { zoneMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(selectedZoneName, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = zoneMenuExpanded, onDismissRequest = { zoneMenuExpanded = false }) {
                                zones.forEach { z ->
                                    DropdownMenuItem(
                                        text = { Text(z.name) },
                                        onClick = { selectedZoneId = z.id; zoneMenuExpanded = false },
                                    )
                                }
                            }
                        }
                    }
                    LabeledField("Motivo", reason) { reason = it }
                    LabeledField("Espacio", spaceNumber) { spaceNumber = it.filter(Char::isDigit) }
                    LabeledField("Monto (₡)", amount) { amount = it }
                }
            }
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                ErrorBanner(uiState.error!!)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (uiState.loading) "Emitiendo..." else "Emitir multa",
                enabled = !uiState.loading,
                onClick = { vm.issue(plate, selectedZoneId, spaceNumber, reason, amount, zones.firstOrNull { it.id == selectedZoneId }?.totalSpots ?: 0) },
            )
            SecondaryButton("Cancelar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────── Admin Alerts ──────────────────────────────────────────

@Composable
fun AdminAlertsScreen(
    onAlertClick: (String) -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminAlertsViewModel,
) {
    val uiState by vm.state.collectAsState()

    Scaffold(bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AvatarBadge("MU", size = 52)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Admin Municipal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Municipal de ${StaticContent.municipality}", color = PrimaryGreen, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            item {
                Text("Notificación de alertas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            if (uiState.error != null) {
                item { ErrorBanner(uiState.error!!) }
            }
            if (!uiState.loading && uiState.error == null && uiState.alerts.isEmpty()) {
                item {
                    Text(
                        "No hay alertas por el momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                }
            }
            items(uiState.alerts) { alert ->
                AlertCard(alert.source, alert.title, alert.body, alert.time) { onAlertClick(alert.id) }
            }
            item {
                DangerButton("Cerrar Sesión", onClick = onLogout)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AlertCard(source: String, title: String, body: String, time: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(BorderColor))
                    Text(source, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Text(time, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ─────────────────── Admin Alert Detail ────────────────────────────────────

@Composable
fun AdminAlertDetailScreen(
    alertId: String,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: AdminAlertsViewModel,
) {
    val uiState by vm.state.collectAsState()
    val alert = uiState.alerts.firstOrNull { it.id == alertId }

    // The feed may still be loading (e.g. opened via a notification deep link).
    if (alert == null) {
        Scaffold(topBar = { EparkTopBar("Alerta", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (uiState.loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Esta alerta ya no está disponible.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        }
        return
    }

    Scaffold(topBar = { EparkTopBar("Alerta", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(BorderColor))
                                Text(alert.source, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Text(alert.time, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(alert.body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        DetailRow("Placa", alert.plate)
                        DetailRow("Zona", alert.zoneName)
                        alert.spaceNumber?.let { DetailRow("Espacio", it) }
                        alert.amount?.let { DetailRow("Monto", it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
