package com.example.myapplication.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.StaticContent
import com.example.myapplication.screens.admin.*
import com.example.myapplication.screens.user.*
import com.example.myapplication.data.repository.AuthState
import com.example.myapplication.ui.admin.AdminZonesViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.payment.PaymentMethodsViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.profile.VehiclesViewModel
import com.example.myapplication.ui.session.ActiveSessionViewModel
import com.example.myapplication.ui.session.SessionConfigViewModel

@Composable
fun EparkNavHost(navController: NavHostController) {
    // Shared mutable state across the nav graph
    var selectedZone by remember { mutableStateOf<ParkingZone?>(null) }

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
    // Scoped here so location/state survives tab navigation
    val homeVm: HomeViewModel = viewModel()
    // Scoped here so SELECT_VEHICLE and SESSION_CONFIG share the same vehicle state
    val sessionConfigVm: SessionConfigViewModel = viewModel()
    // Scoped here so adding a vehicle from any flow refreshes the VEHICLES screen
    val vehiclesVm: VehiclesViewModel = viewModel()

    // Resident bottom bar state
    var residentTab by remember { mutableStateOf(ResidentTab.HOME) }
    // Admin bottom bar state
    var adminTab by remember { mutableStateOf(AdminTab.ZONES) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Refresh data whenever the user returns to screens that list live data
    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            Routes.PAYMENT -> paymentMethodsVm.refresh()
            Routes.VEHICLES -> vehiclesVm.refresh()
            Routes.SESSION_CONFIG -> sessionConfigVm.refresh()
            Routes.SELECT_VEHICLE -> sessionConfigVm.refresh()
        }
    }

    // Determine which bottom bar to show
    val residentRoutes = setOf(
        Routes.USER_HOME, Routes.SESSION_CONFIG, Routes.SELECT_VEHICLE, Routes.PAYMENT,
        Routes.PAYMENT_SUCCESS, Routes.ACTIVE_SESSION, Routes.EXTEND_SESSION,
        Routes.HISTORY, Routes.PROFILE, Routes.EDIT_PROFILE, Routes.VEHICLES,
        Routes.ADD_VEHICLE, Routes.PAYMENT_METHODS, Routes.ADD_PAYMENT,
        Routes.NOTIFICATIONS, Routes.PAY_FINE,
    )
    val adminRoutes = setOf(
        Routes.ADMIN_ZONES, Routes.ADMIN_REPORTS, Routes.ADMIN_FINES, Routes.ADMIN_ALERTS,
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

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

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
            PaymentScreen(
                sessionId = finalizedSessionId,
                totalCost = finalizedCost,
                duration = finalizedDuration,
                onConfirm = { actualCost, invoice ->
                    finalizedCost = actualCost
                    finalizedInvoice = invoice
                    activeSessionVm.clearSession()
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
                onPayFine = { navController.navigate(Routes.PAY_FINE) },
                bottomBar = residentBottomBar,
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
                    AuthState.clear()
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
            )
        }

        composable(Routes.ADD_PAYMENT) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            AddPaymentScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
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
            PayFineScreen(
                onConfirm = {
                    navController.navigate(Routes.PAYMENT_SUCCESS) {
                        popUpTo(Routes.HISTORY)
                    }
                },
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
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
            AdminReportsScreen(bottomBar = adminBottomBar)
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
                    AuthState.clear()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_ALERT_DETAIL) { backStackEntry ->
            LaunchedEffect(Unit) { adminTab = AdminTab.ALERTS }
            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
            AdminAlertDetailScreen(
                alertId = alertId,
                onBack = { navController.popBackStack() },
                bottomBar = adminBottomBar,
            )
        }
    }
}
