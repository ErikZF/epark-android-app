package com.example.myapplication.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.Fine
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.history.HistoryViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun HistoryScreen(
    onPayFine: (Fine) -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: HistoryViewModel = viewModel(),
) {
    var selectedTab by remember { mutableStateOf(0) }
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
                Text("Actividad", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                if (uiState.isOffline) {
                    Spacer(Modifier.height(8.dp))
                    ErrorBanner("Sin conexión — mostrando historial guardado localmente")
                }
                Spacer(Modifier.height(16.dp))
                SegmentedTabs(
                    options = listOf("Sesiones", "Multas"),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                )
                Spacer(Modifier.height(4.dp))
            }
            if (uiState.loading) {
                item { Text("Cargando...", color = TextMuted) }
            }
            uiState.error?.let { msg ->
                item { ErrorBanner(msg) }
            }
            if (selectedTab == 0) {
                items(uiState.sessions) { session ->
                    ActivityRow(session = session)
                }
            } else {
                if (uiState.fines.any { !it.isPaid }) {
                    item {
                        ErrorBanner("Tienes ${uiState.fines.count { !it.isPaid }} multas pendiente de pago")
                    }
                }
                items(uiState.fines) { fine ->
                    FineRow(
                        fine = fine,
                        showPayButton = true,
                        onPayClick = onPayFine,
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
