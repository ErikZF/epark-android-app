package com.example.myapplication.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.StaticContent
import com.example.myapplication.ui.admin.AdminFinesViewModel
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
) {
    var name by remember { mutableStateOf("Zona Norte") }
    var spaces by remember { mutableStateOf("10") }
    var rate by remember { mutableStateOf("1200") }
    var hoursFrom by remember { mutableStateOf("10:00 am") }
    var hoursTo by remember { mutableStateOf("23:00 pm") }

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
                    LabeledField("Espacios", spaces) { spaces = it }
                    LabeledField("Tarifa/hr", rate) { rate = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Horario Desde", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(hoursFrom, { hoursFrom = it }, "10:00 am")
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Horario Hasta", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            EparkTextField(hoursTo, { hoursTo = it }, "23:00 pm")
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Guardar zona", onClick = onSaved)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
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
) {
    var name by remember { mutableStateOf("Zona Norte") }
    var spaces by remember { mutableStateOf("10") }
    var rate by remember { mutableStateOf("1200") }
    var hoursFrom by remember { mutableStateOf("10:00 am") }
    var hoursTo by remember { mutableStateOf("23:00 pm") }

    Scaffold(topBar = { EparkTopBar("Gestionar zona", onBack = onBack) }, bottomBar = bottomBar, containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            // Zone info header card
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
                    StatusChip(if (zone.isActive) "Activa" else "Inactiva")
                }
            }
            Spacer(Modifier.height(16.dp))
            // Edit form card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledField("Nombre", name) { name = it }
                    LabeledField("Espacios", spaces) { spaces = it }
                    LabeledField("Tarifa/hr", rate) { rate = it }
                    Text("Horario Desde", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EparkTextField(hoursFrom, { hoursFrom = it }, "10:00 am", Modifier.weight(1f))
                        EparkTextField(hoursTo, { hoursTo = it }, "23:00 pm", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {}) {
                            Text("Desactivar/ Activar", color = TextSecondary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Confirmar", onClick = onConfirm)
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Buscar por fecha", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Date", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                            Text(report?.date ?: "—", color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item { StatusChip("Hoy") }
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
