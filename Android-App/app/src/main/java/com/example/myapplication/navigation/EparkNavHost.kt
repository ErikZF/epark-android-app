package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.data.AlertPreferences
import com.example.myapplication.data.AuthPreferences
import com.example.myapplication.data.Fine
import com.example.myapplication.data.HistoryCache
import com.example.myapplication.data.NotificationHelper
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.SessionAlarmScheduler
import com.example.myapplication.data.StaticContent
import com.example.myapplication.screens.admin.*
import com.example.myapplication.screens.user.*
import com.example.myapplication.data.repository.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.ui.admin.AdminAlertsViewModel
import com.example.myapplication.ui.admin.AdminZonesViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.history.HistoryViewModel
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.payment.PaymentMethodsViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.VehiclesViewModel
import com.example.myapplication.ui.session.ActiveSessionViewModel
import com.example.myapplication.ui.session.SessionConfigViewModel

@Composable
fun EparkNavHost(
    navController: NavHostController,
    // Deep-link target from a tapped notification (e.g. "admin_alert_detail"), with an
    // optional alert id. Set by MainActivity; consumed once the user is on an admin route.
    deepLinkTarget: String? = null,
    deepLinkAlertId: String? = null,
    onDeepLinkHandled: () -> Unit = {},
) {
    // Shared mutable state across the nav graph
    var selectedZone by remember { mutableStateOf<ParkingZone?>(null) }

    // Fine passed from History → PayFine → FinePaymentSuccess
    var selectedFine by remember { mutableStateOf<Fine?>(null) }
    var fineInvoice by remember { mutableStateOf<String?>(null) }

    // Finalized session data passed from ActiveSession → Payment → PaymentSuccess
    var finalizedSessionId by remember { mutableStateOf(-1) }
    var finalizedCost by remember { mutableStateOf(0.0) }
    var finalizedDuration by remember { mutableStateOf("") }
    var finalizedZoneName by remember { mutableStateOf("") }
    var finalizedInvoice by remember { mutableStateOf<String?>(null) }

    // Shared ViewModel so ActiveSession and ExtendSession interact with the same state
    val activeSessionVm: ActiveSessionViewModel = viewModel()

    // Scoped here so vehicle adds can trigger a profile refresh on back-navigation
    val profileVm: ProfileViewModel = viewModel()
    // Scoped here so returning from ADD_PAYMENT triggers a methods refresh on PaymentScreen
    val paymentMethodsVm: PaymentMethodsViewModel = viewModel()
    // Scoped here so zone edits/creates trigger a list refresh on return
    val adminZonesVm: AdminZonesViewModel = viewModel()
    // Scoped here so the alerts list is shared between the list, the detail screen,
    // and the app-wide polling effect that raises system notifications.
    val adminAlertsVm: AdminAlertsViewModel = viewModel()
    // Scoped here so location/state survives tab navigation
    val homeVm: HomeViewModel = viewModel()
    // Scoped here so SELECT_VEHICLE and SESSION_CONFIG share the same vehicle state
    val sessionConfigVm: SessionConfigViewModel = viewModel()
    // Scoped here so adding a vehicle from any flow refreshes the VEHICLES screen
    val vehiclesVm: VehiclesViewModel = viewModel()
    // Scoped here so fine payment can trigger a refresh on the history list
    val historyVm: HistoryViewModel = viewModel()

    val coroutineScope = rememberCoroutineScope()

    // Resident bottom bar state
    var residentTab by remember { mutableStateOf(ResidentTab.HOME) }
    // Admin bottom bar state
    var adminTab by remember { mutableStateOf(AdminTab.ZONES) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Refresh data whenever the user returns to screens that list live data
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.PAYMENT -> paymentMethodsVm.refresh()
            Routes.PAYMENT_METHODS -> paymentMethodsVm.refresh()
            Routes.VEHICLES -> vehiclesVm.refresh()
            Routes.SESSION_CONFIG -> sessionConfigVm.refresh()
            Routes.SELECT_VEHICLE -> sessionConfigVm.refresh()
            Routes.HISTORY -> historyVm.refresh()
            Routes.PROFILE -> profileVm.refresh()
            Routes.ADMIN_ZONES -> adminZonesVm.refresh()
            Routes.ADMIN_ALERTS -> adminAlertsVm.refresh()
        }
    }

    val context = LocalContext.current
    val alertsState by adminAlertsVm.state.collectAsState()
    // Avoids spamming the tray with the historical backlog present on first load.
    var alertsSeeded by remember { mutableStateOf(false) }

    // Poll the admin notification feed while an admin is logged in.
    LaunchedEffect(AuthState.role) {
        if (AuthState.role != "admin") return@LaunchedEffect
        while (true) {
            adminAlertsVm.refresh()
            delay(30_000)
        }
    }

    // Raise a local notification for each feed item not previously surfaced. The first
    // populated feed only seeds the "seen" set so the backlog doesn't flood the tray.
    LaunchedEffect(alertsState.alerts, alertsState.loading) {
        if (AuthState.role != "admin" || alertsState.loading) return@LaunchedEffect
        val alerts = alertsState.alerts
        val seen = AlertPreferences.seenAlertIds
        if (!alertsSeeded) {
            AlertPreferences.seenAlertIds = seen + alerts.map { it.id }
            alertsSeeded = true
            return@LaunchedEffect
        }
        val newOnes = alerts.filter { it.id !in seen }
        newOnes.forEach { NotificationHelper.showAdminAlert(context, it.title, it.body, it.id) }
        if (newOnes.isNotEmpty()) AlertPreferences.seenAlertIds = seen + newOnes.map { it.id }
    }

    // Consume a notification deep link once splash routing has completed and the role
    // is known. onDeepLinkHandled clears it so route changes don't re-trigger navigation.
    LaunchedEffect(deepLinkTarget, deepLinkAlertId, currentRoute) {
        val target = deepLinkTarget ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute == Routes.SPLASH || currentRoute == Routes.LOGIN) return@LaunchedEffect
        var handled = true
        when (target) {
            "admin_alert_detail" -> if (AuthState.role == "admin") {
                if (!deepLinkAlertId.isNullOrBlank()) navController.navigate(Routes.adminAlertDetail(deepLinkAlertId))
                else navController.navigate(Routes.ADMIN_ALERTS)
            } else handled = false
            "admin_alerts" -> if (AuthState.role == "admin") navController.navigate(Routes.ADMIN_ALERTS) else handled = false
            "active_session" -> if (AuthState.role != "admin") navController.navigate(Routes.ACTIVE_SESSION) else handled = false
            else -> handled = false
        }
        if (handled) onDeepLinkHandled()
    }

    // Determine which bottom bar to show
    val residentRoutes = setOf(
        Routes.USER_HOME, Routes.SESSION_CONFIG, Routes.SELECT_VEHICLE, Routes.PAYMENT,
        Routes.PAYMENT_SUCCESS, Routes.ACTIVE_SESSION, Routes.EXTEND_SESSION,
        Routes.HISTORY, Routes.PROFILE, Routes.EDIT_PROFILE, Routes.VEHICLES,
        Routes.ADD_VEHICLE, Routes.PAYMENT_METHODS, Routes.ADD_PAYMENT,
        Routes.NOTIFICATIONS, Routes.PAY_FINE, Routes.FINE_PAYMENT_SUCCESS,
    )
    val adminRoutes = setOf(
        Routes.ADMIN_ZONES, Routes.ADMIN_REPORTS, Routes.ADMIN_LOGS, Routes.ADMIN_FINES, Routes.ADMIN_ALERTS,
        Routes.ADMIN_ADD_ZONE, Routes.ADMIN_ALERT_DETAIL, Routes.ADMIN_ISSUE_FINE,
    ) + setOf(Routes.ADMIN_MANAGE_ZONE.substringBefore("{"))

    val showResidentBar = currentRoute in residentRoutes
    val showAdminBar = adminRoutes.any { currentRoute?.startsWith(it.trimEnd('/')) == true } ||
        currentRoute?.startsWith("admin_manage_zone") == true ||
        currentRoute?.startsWith("admin_alert_detail") == true

    val residentBottomBar: @Composable () -> Unit = {
        if (showResidentBar) {
            ResidentBottomBar(
                selected = residentTab,
                onSelect = { tab ->
                    residentTab = tab
                    when (tab) {
                        ResidentTab.HOME -> navController.navigate(Routes.USER_HOME) {
                            popUpTo(Routes.USER_HOME) { inclusive = true }
                        }
                        ResidentTab.SESSION -> navController.navigate(Routes.SESSION_CONFIG) {
                            popUpTo(Routes.USER_HOME)
                        }
                        ResidentTab.HISTORY -> navController.navigate(Routes.HISTORY) {
                            popUpTo(Routes.USER_HOME)
                        }
                        ResidentTab.PROFILE -> navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.USER_HOME)
                        }
                    }
                },
            )
        }
    }

    val adminBottomBar: @Composable () -> Unit = {
        if (showAdminBar) {
            AdminBottomBar(
                selected = adminTab,
                onSelect = { tab ->
                    adminTab = tab
                    when (tab) {
                        AdminTab.ZONES -> navController.navigate(Routes.ADMIN_ZONES) {
                            popUpTo(Routes.ADMIN_ZONES) { inclusive = true }
                        }
                        AdminTab.REPORTS -> navController.navigate(Routes.ADMIN_REPORTS) {
                            popUpTo(Routes.ADMIN_ZONES)
                        }
                        AdminTab.FINES -> navController.navigate(Routes.ADMIN_FINES) {
                            popUpTo(Routes.ADMIN_ZONES)
                        }
                        AdminTab.ALERTS -> navController.navigate(Routes.ADMIN_ALERTS) {
                            popUpTo(Routes.ADMIN_ZONES)
                        }
                    }
                },
            )
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // ── Splash / session restore ──────────────────────────────────────
        composable(Routes.SPLASH) {
            LaunchedEffect(Unit) {
                val stored = AuthPreferences.load()
                if (stored != null) {
                    AuthState.set(stored)
                    if (stored.role != "admin") {
                        activeSessionVm.loadActiveSession()
                        profileVm.refresh()
                        homeVm.refresh()
                    }
                    val destination = if (stored.role == "admin") Routes.ADMIN_ZONES else Routes.USER_HOME
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // ── Auth ──────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { role ->
                    if (role != "admin") {
                        activeSessionVm.loadActiveSession()
                        profileVm.refresh()
                        homeVm.refresh()
                    }
                    val destination = if (role == "admin") Routes.ADMIN_ZONES else Routes.USER_HOME
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Routes.VEHICLE_REGISTER) },
                onLoginClick = { navController.navigate(Routes.LOGIN) },
            )
        }

        composable(Routes.VEHICLE_REGISTER) {
            VehicleRegisterScreen(
                onRegistered = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onExit = { navController.popBackStack() },
            )
        }

        composable(Routes.EXIT) {
            ExitLauncherScreen(onBack = { navController.navigate(Routes.LOGIN) })
        }

        // ── Resident Main ─────────────────────────────────────────────────
        composable(Routes.USER_HOME) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.HOME }
            HomeScreen(
                onZoneClick = { zone ->
                    selectedZone = zone
                    navController.navigate(Routes.SESSION_CONFIG)
                },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                bottomBar = residentBottomBar,
                vm = homeVm,
            )
        }

        composable(Routes.SESSION_CONFIG) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            // Si ya hay una sesión activa del usuario actual, redirigir automáticamente
            val activeState by activeSessionVm.state.collectAsState()
            LaunchedEffect(activeState.session) {
                val session = activeState.session
                if (session != null && AuthState.userId > 0 && !activeState.loading) {
                    navController.navigate(Routes.ACTIVE_SESSION) {
                        popUpTo(Routes.SESSION_CONFIG) { inclusive = true }
                    }
                }
            }
            val zone = selectedZone ?: StaticContent.placeholderZone
            SessionConfigScreen(
                zone = zone,
                onStartParking = {
                    activeSessionVm.loadActiveSession()
                    navController.navigate(Routes.ACTIVE_SESSION) {
                        popUpTo(Routes.SESSION_CONFIG) { inclusive = true }
                    }
                },
                onSelectOtherVehicle = { navController.navigate(Routes.SELECT_VEHICLE) },
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
                vm = sessionConfigVm,
            )
        }

        composable(Routes.ACTIVE_SESSION) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            ActiveSessionScreen(
                onFinalized = { sessionId, cost, duration ->
                    finalizedSessionId = sessionId
                    finalizedCost = cost
                    finalizedDuration = duration
                    finalizedZoneName = activeSessionVm.state.value.session?.zoneName ?: ""
                    navController.navigate(Routes.PAYMENT)
                },
                onExtend = { navController.navigate(Routes.EXTEND_SESSION) },
                bottomBar = residentBottomBar,
                vm = activeSessionVm,
            )
        }

        composable(Routes.EXTEND_SESSION) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            ExtendSessionScreen(
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
                vm = activeSessionVm,
            )
        }

        composable(Routes.PAYMENT) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            val paymentContext = LocalContext.current
            PaymentScreen(
                sessionId = finalizedSessionId,
                totalCost = finalizedCost,
                duration = finalizedDuration,
                onConfirm = { actualCost, invoice ->
                    finalizedCost = actualCost
                    finalizedInvoice = invoice
                    // La sesión terminó: cancelamos la alarma de vencimiento pendiente.
                    SessionAlarmScheduler.cancel(paymentContext)
                    activeSessionVm.clearSession()
                    profileVm.refresh()
                    historyVm.refresh()
                    navController.navigate(Routes.PAYMENT_SUCCESS)
                },
                onBack = { navController.popBackStack() },
                onAddCard = { navController.navigate(Routes.ADD_PAYMENT) },
                bottomBar = residentBottomBar,
                methodsVm = paymentMethodsVm,
            )
        }

        composable(Routes.PAYMENT_SUCCESS) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            PaymentSuccessScreen(
                totalCost = finalizedCost,
                duration = finalizedDuration,
                zoneName = finalizedZoneName,
                invoiceNumber = finalizedInvoice,
                onNewSession = {
                    activeSessionVm.loadActiveSession()
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) { inclusive = true }
                    }
                },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.HISTORY) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.HISTORY }
            HistoryScreen(
                onPayFine = { fine ->
                    selectedFine = fine
                    navController.navigate(Routes.PAY_FINE)
                },
                bottomBar = residentBottomBar,
                vm = historyVm,
            )
        }

        // ── Profile cluster ───────────────────────────────────────────────
        composable(Routes.PROFILE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            ProfileScreen(
                onEdit = { navController.navigate(Routes.EDIT_PROFILE) },
                onVehicles = { navController.navigate(Routes.VEHICLES) },
                onPaymentMethods = { navController.navigate(Routes.PAYMENT_METHODS) },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onLogout = {
                    coroutineScope.launch { AuthPreferences.clear() }
                    AuthState.clear()
                    HistoryCache.clear()
                    activeSessionVm.clearSession()
                    profileVm.clear()
                    homeVm.selectMunicipality(null)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = residentBottomBar,
                vm = profileVm,
            )
        }

        composable(Routes.EDIT_PROFILE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.VEHICLES) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            VehiclesScreen(
                onBack = { navController.popBackStack() },
                onAddVehicle = { navController.navigate(Routes.ADD_VEHICLE) },
                bottomBar = residentBottomBar,
                vm = vehiclesVm,
            )
        }

        composable(Routes.SELECT_VEHICLE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            SelectVehicleScreen(
                onBack = { navController.popBackStack() },
                onAddVehicle = { navController.navigate(Routes.ADD_VEHICLE) },
                bottomBar = residentBottomBar,
                vm = sessionConfigVm,
            )
        }

        composable(Routes.ADD_VEHICLE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            AddVehicleScreen(
                onBack = { navController.popBackStack() },
                onAdded = {
                    profileVm.refresh()
                    vehiclesVm.refresh()
                    sessionConfigVm.refresh()
                    navController.popBackStack()
                },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.PAYMENT_METHODS) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            PaymentMethodsScreen(
                onBack = { navController.popBackStack() },
                onAddCard = { navController.navigate(Routes.ADD_PAYMENT) },
                bottomBar = residentBottomBar,
                vm = paymentMethodsVm,
            )
        }

        composable(Routes.ADD_PAYMENT) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            AddPaymentScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    paymentMethodsVm.refresh()
                    navController.popBackStack()
                },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.PAY_FINE) {
            selectedFine?.let { fine ->
                PayFineScreen(
                    fine = fine,
                    onConfirm = { invoice ->
                        fineInvoice = invoice
                        historyVm.refresh()
                        profileVm.refresh()
                        navController.navigate(Routes.FINE_PAYMENT_SUCCESS) {
                            popUpTo(Routes.HISTORY)
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onAddCard = { navController.navigate(Routes.ADD_PAYMENT) },
                    bottomBar = residentBottomBar,
                    methodsVm = paymentMethodsVm,
                )
            }
        }

        composable(Routes.FINE_PAYMENT_SUCCESS) {
            selectedFine?.let { fine ->
                FinePaymentSuccessScreen(
                    zoneName = fine.zoneName,
                    spaceNumber = fine.spaceNumber,
                    reason = fine.reason,
                    totalPaid = fine.amount,
                    invoiceNumber = fineInvoice,
                    onBack = {
                        navController.navigate(Routes.HISTORY) {
                            popUpTo(Routes.HISTORY) { inclusive = true }
                        }
                    },
                    bottomBar = residentBottomBar,
                )
            }
        }

        // ── Admin ─────────────────────────────────────────────────────────
        composable(Routes.ADMIN_ZONES) {
            LaunchedEffect(Unit) { adminTab = AdminTab.ZONES }
            AdminZonesScreen(
                onManageZone = { zone ->
                    selectedZone = zone
                    navController.navigate(Routes.adminManageZone(zone.id))
                },
                onAddZone = { navController.navigate(Routes.ADMIN_ADD_ZONE) },
                bottomBar = adminBottomBar,
                vm = adminZonesVm,
            )
        }

        composable(Routes.ADMIN_ADD_ZONE) {
            LaunchedEffect(Unit) { adminTab = AdminTab.ZONES }
            AdminAddZoneScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    adminZonesVm.refresh()
                    navController.navigate(Routes.ADMIN_ZONES) {
                        popUpTo(Routes.ADMIN_ZONES) { inclusive = true }
                    }
                },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_MANAGE_ZONE) { backStackEntry ->
            LaunchedEffect(Unit) { adminTab = AdminTab.ZONES }
            val zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
            val zone = selectedZone?.takeIf { it.id == zoneId } ?: selectedZone ?: StaticContent.placeholderZone
            AdminManageZoneScreen(
                zone = zone,
                onBack = { navController.popBackStack() },
                onConfirm = {
                    adminZonesVm.refresh()
                    navController.popBackStack()
                },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_REPORTS) {
            LaunchedEffect(Unit) { adminTab = AdminTab.REPORTS }
            AdminReportsScreen(
                onViewLogs = { navController.navigate(Routes.ADMIN_LOGS) },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_LOGS) {
            LaunchedEffect(Unit) { adminTab = AdminTab.REPORTS }
            AdminLogsScreen(
                onBack = { navController.popBackStack() },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_FINES) {
            LaunchedEffect(Unit) { adminTab = AdminTab.FINES }
            AdminFinesScreen(
                onIssueFine = { navController.navigate(Routes.ADMIN_ISSUE_FINE) },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_ISSUE_FINE) {
            LaunchedEffect(Unit) { adminTab = AdminTab.FINES }
            val zonesState by adminZonesVm.state.collectAsState()
            AdminIssueFineScreen(
                zones = zonesState.zones,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.ADMIN_FINES) {
                        popUpTo(Routes.ADMIN_FINES) { inclusive = true }
                    }
                },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_ALERTS) {
            LaunchedEffect(Unit) { adminTab = AdminTab.ALERTS }
            AdminAlertsScreen(
                onAlertClick = { alertId -> navController.navigate(Routes.adminAlertDetail(alertId)) },
                onLogout = {
                    coroutineScope.launch { AuthPreferences.clear() }
                    AuthState.clear()
                    HistoryCache.clear()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = adminBottomBar,
                vm = adminAlertsVm,
            )
        }

        composable(Routes.ADMIN_ALERT_DETAIL) { backStackEntry ->
            LaunchedEffect(Unit) { adminTab = AdminTab.ALERTS }
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AdminAlertDetailScreen(
                alertId = alertId,
                onBack = { navController.popBackStack() },
                bottomBar = adminBottomBar,
                vm = adminAlertsVm,
            )
        }
    }
}
