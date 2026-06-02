package com.example.myapplication.screens.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    onZoneClick: (ParkingZone) -> Unit,
    onNotificationsClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var search by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()
    var municipalityMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Tracks what to tell the user about geolocation.
    var locationStatus by remember { mutableStateOf(LocationStatus.REQUESTING) }

    // High-accuracy fresh fix so "nearby" reflects the device's actual position.
    fun requestLocation() {
        locationStatus = LocationStatus.REQUESTING
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    vm.updateLocation(location.latitude, location.longitude)
                    locationStatus = LocationStatus.ACTIVE
                } else {
                    locationStatus = LocationStatus.UNAVAILABLE
                }
            }
            .addOnFailureListener { locationStatus = LocationStatus.UNAVAILABLE }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestLocation() else locationStatus = LocationStatus.DENIED
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            requestLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val zones = uiState.zones.filter {
        search.isBlank() || it.name.contains(search, ignoreCase = true)
    }

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
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Municipalidad", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { municipalityMenuExpanded = true },
                            ) {
                                Text(
                                    uiState.selectedMunicipality ?: "Todas",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                            }
                            DropdownMenu(
                                expanded = municipalityMenuExpanded,
                                onDismissRequest = { municipalityMenuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todas") },
                                    onClick = {
                                        vm.selectMunicipality(null)
                                        municipalityMenuExpanded = false
                                    },
                                    leadingIcon = if (uiState.selectedMunicipality == null) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null,
                                )
                                uiState.municipalities.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            vm.selectMunicipality(name)
                                            municipalityMenuExpanded = false
                                        },
                                        leadingIcon = if (uiState.selectedMunicipality == name) {
                                            { Icon(Icons.Default.Check, contentDescription = null) }
                                        } else null,
                                    )
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceWhite),
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = TextSecondary)
                    }
                }
            }
            item {
                SearchField(value = search, onValueChange = { search = it }, placeholder = "Buscar zona de parqueo")
            }
            item {
                Text("Zonas cercanas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LocationStatusRow(
                    status = locationStatus,
                    lat = uiState.userLat,
                    lon = uiState.userLon,
                    onEnableClick = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                )
            }
            if (uiState.loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            uiState.error?.let { msg ->
                item { Text(msg, color = MaterialTheme.colorScheme.error) }
            }
            items(zones) { zone ->
                val distanceMeters = uiState.distanceTo(zone)
                val distanceLabel = distanceMeters?.let {
                    if (it < 1000f) "${"%.0f".format(it)} m" else "${"%.1f".format(it / 1000f)} km"
                }
                ZoneCard(zone = zone, distance = distanceLabel, onClick = { onZoneClick(zone) })
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

private enum class LocationStatus { REQUESTING, ACTIVE, DENIED, UNAVAILABLE }

@Composable
private fun LocationStatusRow(
    status: LocationStatus,
    lat: Double?,
    lon: Double?,
    onEnableClick: () -> Unit,
) {
    when {
        status == LocationStatus.ACTIVE && lat != null && lon != null -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                Text(
                    "Ordenadas por cercanía · ${"%.5f".format(lat)}, ${"%.5f".format(lon)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen,
                )
            }
        }
        status == LocationStatus.REQUESTING -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationSearching, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text("Obteniendo tu ubicación…", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        else -> {
            // DENIED or UNAVAILABLE: explain and let the user retry.
            val message = if (status == LocationStatus.DENIED) {
                "Activa la ubicación para ordenar las zonas por cercanía"
            } else {
                "No se pudo obtener tu ubicación"
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                Text(
                    "Activar",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onEnableClick),
                )
            }
        }
    }
}
