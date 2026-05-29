package com.example.myapplication.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.StaticContent
import com.example.myapplication.screens.admin.*
import com.example.myapplication.screens.user.*
import com.example.myapplication.ui.components.*

@Composable
fun EparkNavHost(navController: NavHostController) {
    // Shared mutable state across the nav graph
    var selectedZone by remember { mutableStateOf<ParkingZone?>(null) }

    // Resident bottom bar state
    var residentTab by remember { mutableStateOf(ResidentTab.HOME) }
    // Admin bottom bar state
    var adminTab by remember { mutableStateOf(AdminTab.ZONES) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    // Determine which bottom bar to show
    val residentRoutes = setOf(
        Routes.USER_HOME, Routes.SESSION_CONFIG, Routes.PAYMENT,
        Routes.PAYMENT_SUCCESS, Routes.ACTIVE_SESSION, Routes.EXTEND_SESSION,
        Routes.HISTORY, Routes.PROFILE, Routes.EDIT_PROFILE, Routes.ADD_VEHICLE,
        Routes.PAYMENT_METHODS, Routes.ADD_PAYMENT, Routes.NOTIFICATIONS, Routes.PAY_FINE,
    )
    val adminRoutes = setOf(
        Routes.ADMIN_ZONES, Routes.ADMIN_REPORTS, Routes.ADMIN_FINES, Routes.ADMIN_ALERTS,
        Routes.ADMIN_ADD_ZONE, Routes.ADMIN_ALERT_DETAIL,
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
            )
        }

        composable(Routes.SESSION_CONFIG) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            val zone = selectedZone ?: StaticContent.placeholderZone
            SessionConfigScreen(
                zone = zone,
                onStartParking = { navController.navigate(Routes.PAYMENT) },
                onSelectOtherVehicle = {},
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.PAYMENT) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            val zone = selectedZone ?: StaticContent.placeholderZone
            PaymentScreen(
                zone = zone,
                onConfirm = { navController.navigate(Routes.PAYMENT_SUCCESS) },
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.PAYMENT_SUCCESS) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            PaymentSuccessScreen(
                onNewSession = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.USER_HOME) { inclusive = true }
                    }
                },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.ACTIVE_SESSION) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            ActiveSessionScreen(
                onFinalize = { navController.navigate(Routes.PAYMENT) },
                onExtend = { navController.navigate(Routes.EXTEND_SESSION) },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.EXTEND_SESSION) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.SESSION }
            ExtendSessionScreen(
                onAccept = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
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
                onAddVehicle = { navController.navigate(Routes.ADD_VEHICLE) },
                onPaymentMethods = { navController.navigate(Routes.PAYMENT_METHODS) },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.EDIT_PROFILE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                bottomBar = residentBottomBar,
            )
        }

        composable(Routes.ADD_VEHICLE) {
            LaunchedEffect(Unit) { residentTab = ResidentTab.PROFILE }
            AddVehicleScreen(
                onBack = { navController.popBackStack() },
                onAdded = { navController.popBackStack() },
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
            )
        }

        composable(Routes.ADMIN_ADD_ZONE) {
            LaunchedEffect(Unit) { adminTab = AdminTab.ZONES }
            AdminAddZoneScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
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
                onConfirm = { navController.popBackStack() },
                bottomBar = adminBottomBar,
            )
        }

        composable(Routes.ADMIN_REPORTS) {
            LaunchedEffect(Unit) { adminTab = AdminTab.REPORTS }
            AdminReportsScreen(bottomBar = adminBottomBar)
        }

        composable(Routes.ADMIN_FINES) {
            LaunchedEffect(Unit) { adminTab = AdminTab.FINES }
            AdminFinesScreen(bottomBar = adminBottomBar)
        }

        composable(Routes.ADMIN_ALERTS) {
            LaunchedEffect(Unit) { adminTab = AdminTab.ALERTS }
            AdminAlertsScreen(
                onAlertClick = { alertId -> navController.navigate(Routes.adminAlertDetail(alertId)) },
                onLogout = {
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
