package com.example.myapplication.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.StaticContent
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun HomeScreen(
    onZoneClick: (ParkingZone) -> Unit,
    onNotificationsClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var search by remember { mutableStateOf("") }
    val uiState by vm.state.collectAsState()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(StaticContent.municipality, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
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
                ZoneCard(zone = zone, onClick = { onZoneClick(zone) })
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
