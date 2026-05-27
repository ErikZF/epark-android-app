package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.Vehicle
import com.example.myapplication.ui.theme.*

// ──────────────────────────── Brand header (auth screens) ────────────────────

@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(listOf(MintLight, SurfaceWhite))
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(MintAccent)
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-60).dp, y = 30.dp)
                .clip(CircleShape)
                .background(MintAccent)
        )
        LogoBadge(
            modifier = Modifier
                .padding(start = 24.dp, top = 40.dp)
                .align(Alignment.TopStart)
        )
    }
}

@Composable
fun LogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "e",
            color = SurfaceWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BrandTitle(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "e-park",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Parqueo inteligente para tu ciudad",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
    }
}

// ──────────────────────────── Auth tab switcher ───────────────────────────────

@Composable
fun AuthTabs(
    selectedLogin: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEEEEEE))
            .padding(4.dp),
    ) {
        AuthTab("Iniciar sesión", selectedLogin, onLoginClick, Modifier.weight(1f))
        AuthTab("Registrarse", !selectedLogin, onRegisterClick, Modifier.weight(1f))
    }
}

@Composable
private fun AuthTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) PrimaryGreen else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) SurfaceWhite else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

// ──────────────────────────── Inputs ─────────────────────────────────────────

@Composable
fun EparkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = BorderColor,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            disabledContainerColor = SurfaceWhite,
            disabledBorderColor = BorderColor,
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), clip = false),
    )
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ──────────────────────────── Buttons ────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = TextSecondary,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, color = textColor, fontSize = 16.sp)
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PendingRed),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD0D0)),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(LightRed, RoundedCornerShape(14.dp)),
    ) {
        Text(text, color = PendingRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = PrimaryGreen,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.clickable(onClick = onClick),
    )
}

// ──────────────────────────── Status chip ────────────────────────────────────

@Composable
fun StatusChip(text: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (text.lowercase()) {
        "pagado", "activa", "cobrada" -> MintAccent to PrimaryGreen
        "pendiente" -> Color(0xFFFFE5E5) to PendingRed
        "lleno" -> Color(0xFFFFE5E5) to PendingRed
        "inactiva" -> Color(0xFFE0E0E0) to SurfaceDark
        else -> MintAccent to PrimaryGreen
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

// ──────────────────────────── Zone card ──────────────────────────────────────

@Composable
fun ZoneCard(zone: ParkingZone, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spotsLabel = when {
        zone.freeSpots == 0 -> "Lleno"
        else -> "${zone.freeSpots} libres"
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MintAccent),
                contentAlignment = Alignment.Center,
            ) {
                Text("P", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(zone.rate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            StatusChip(spotsLabel)
        }
    }
}

// ──────────────────────────── Admin zone card ─────────────────────────────────

@Composable
fun AdminZoneCard(
    zone: ParkingZone,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MintAccent),
                contentAlignment = Alignment.Center,
            ) {
                Text("P", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(zone.rate, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusChip(if (zone.isActive) "Activa" else "Inactiva")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onManageClick),
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppBackground)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text("Gestionar", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ──────────────────────────── Vehicle card ───────────────────────────────────

@Composable
fun VehicleCard(vehicle: Vehicle, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text(vehicle.plate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(16.dp))
            Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

// ──────────────────────────── Payment method card ────────────────────────────

@Composable
fun PaymentMethodCard(
    method: PaymentMethod,
    selected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) PrimaryGreen else BorderColor
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .then(if (onSelect != null) Modifier.clickable(onClick = onSelect) else Modifier),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${method.label} ******* ${method.last4}", style = MaterialTheme.typography.titleSmall)
                Text("Vence ${method.expiry}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ──────────────────────────── Activity / session row ─────────────────────────

@Composable
fun ActivityRow(session: ParkingSession, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(session.zoneName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${session.date} ${session.plate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(session.total, color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
                StatusChip(session.status)
            }
        }
    }
}

// ──────────────────────────── Fine row ───────────────────────────────────────

@Composable
fun FineRow(
    fine: Fine,
    showPayButton: Boolean = false,
    onPayClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fine.zoneName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Espacio ${fine.spaceNumber}  ${fine.date}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    if (!fine.plate.isEmpty()) {
                        Text("Placa ${fine.plate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(fine.amount, color = if (fine.isPaid) PrimaryGreen else PendingRed, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    StatusChip(if (fine.isPaid) "Pagado" else "Pendiente")
                }
            }
            if (showPayButton && !fine.isPaid && onPayClick != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PendingRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                ) {
                    Text("Pagar multa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ──────────────────────────── Success receipt ─────────────────────────────────

@Composable
fun SuccessReceipt(
    title: String,
    subtitle: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MintAccent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEachIndexed { i, (label, value) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
                        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    if (i < rows.lastIndex) HorizontalDivider(color = DividerColor)
                }
            }
        }
    }
}

// ──────────────────────────── Stat card ──────────────────────────────────────

@Composable
fun StatCard(icon: ImageVector, label: String, value: String, iconTint: Color = PrimaryGreen, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
    }
}

// ──────────────────────────── Notification card ───────────────────────────────

@Composable
fun NotificationCard(title: String, body: String, time: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MintAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(time, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

// ──────────────────────────── Bottom navigation bars ─────────────────────────

enum class ResidentTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    SESSION("Sesión", Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar),
    HISTORY("Historial", Icons.Outlined.History, Icons.Filled.History),
    PROFILE("Perfil", Icons.Outlined.Person, Icons.Filled.Person),
}

enum class AdminTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    ZONES("Zonas", Icons.Outlined.Map, Icons.Filled.Map),
    REPORTS("Reportes", Icons.Outlined.Assessment, Icons.Filled.Assessment),
    FINES("Multas", Icons.Outlined.Warning, Icons.Filled.Warning),
    ALERTS("Alertas", Icons.Outlined.NotificationsActive, Icons.Filled.NotificationsActive),
}

@Composable
fun ResidentBottomBar(
    selected: ResidentTab,
    onSelect: (ResidentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = modifier,
    ) {
        ResidentTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        if (isSelected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = MintLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}

@Composable
fun AdminBottomBar(
    selected: AdminTab,
    onSelect: (AdminTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = SurfaceWhite,
        modifier = modifier,
    ) {
        AdminTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        if (isSelected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = MintLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}

// ──────────────────────────── Generic top app bar ────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EparkTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
    )
}

// ──────────────────────────── Orange warning banner ───────────────────────────

@Composable
fun WarningBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LightOrange)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = WarningOrange)
    }
}

// ──────────────────────────── Error banner ────────────────────────────────────

@Composable
fun ErrorBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LightRed)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = PendingRed, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = PendingRed)
    }
}

// ──────────────────────────── Success dialog ─────────────────────────────────

@Composable
fun SuccessDialog(
    title: String,
    message: String? = null,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryGreen)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(28.dp))
            }
            Text(title, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (message != null) {
                Text(message, color = SurfaceWhite, textAlign = TextAlign.Center, fontSize = 14.sp)
            }
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(primaryLabel, color = TextSecondary)
            }
            if (secondaryLabel != null && onSecondary != null) {
                Button(
                    onClick = onSecondary,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(secondaryLabel, color = TextSecondary)
                }
            }
        }
    }
}

// ──────────────────────────── Avatar badge ───────────────────────────────────

@Composable
fun AvatarBadge(initials: String, size: Int = 52, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.27).dp))
            .background(MintAccent)
            .border(2.dp, PrimaryGreen, RoundedCornerShape((size * 0.27).dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = PrimaryGreen,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.35).sp,
        )
    }
}

// ──────────────────────────── Payment summary card ───────────────────────────

@Composable
fun PaymentSummaryCard(total: String, spaceNumber: String, duration: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total a pagar", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(total, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text("Espacio $spaceNumber", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("$duration de uso", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

// ──────────────────────────── Segmented tabs ─────────────────────────────────

@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) SurfaceWhite else Color.Transparent)
                    .border(1.dp, if (selected) BorderColor else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) TextPrimary else TextSecondary,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
