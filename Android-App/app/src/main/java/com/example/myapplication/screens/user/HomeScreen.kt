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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let { vm.updateLocation(it.latitude, it.longitude) }
                }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let { vm.updateLocation(it.latitude, it.longitude) }
                }
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
