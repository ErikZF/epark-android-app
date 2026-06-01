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
import com.example.myapplication.ui.admin.AdminFinesViewModel
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
            item { SearchField(value = search, onValueChange = { search = it }, placeholder = "Buscar zona de parqueo") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Zonas de parqueo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    TextLink("+ Nueva zona", onClick = onAddZone)
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
                    vm.save(AuthState.municipalityId, name.trim(), description.trim(), spaces, rate, latitude, longitude)
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

    LaunchedEffect(uiState.success) {
        if (uiState.success) onConfirm()
    }

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
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (uiState.loading) "Guardando..." else "Confirmar",
                enabled = !uiState.loading,
                onClick = {
                    vm.save(zone.id, name, spaces, rate, openHour, closeHour, isActive)
                },
            )
            SecondaryButton("Cancelar", onClick = onBack)
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────── Admin Reports ────────────────────────────────────────

@Composable
fun AdminReportsScreen(
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
                Text("Reportes", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
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
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ─────────────────── Admin Fines ──────────────────────────────────────────

@Composable
fun AdminFinesScreen(
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
                Text("Multas", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
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

// ─────────────────── Admin Alerts ──────────────────────────────────────────

@Composable
fun AdminAlertsScreen(
    onAlertClick: (String) -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
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
                Text("Notificacón de alertas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            items(StaticContent.adminAlerts) { alert ->
                AlertCard(alert.source, alert.title, alert.body, alert.time) { onAlertClick(alert.id) }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, "Anterior", tint = TextMuted) }
                    IconButton(onClick = {}) { Icon(Icons.Default.ArrowForward, "Siguiente", tint = TextMuted) }
                }
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
) {
    val alert = StaticContent.adminAlerts.firstOrNull { it.id == alertId } ?: StaticContent.adminAlerts.first()

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
                        Text(alert.body.repeat(3), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(16.dp))
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
