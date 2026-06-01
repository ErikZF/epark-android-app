package com.example.myapplication.navigation

object Routes {
    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val REGISTER_SUCCESS = "register_success"
    const val EXIT = "exit"

    // Vehicle onboarding
    const val VEHICLE_REGISTER = "vehicle_register"
    const val VEHICLE_REGISTER_SUCCESS = "vehicle_register_success"

    // User main
    const val USER_HOME = "user_home"
    const val SESSION_CONFIG = "session_config"
    const val PAYMENT = "payment"
    const val PAYMENT_SUCCESS = "payment_success"
    const val ACTIVE_SESSION = "active_session"
    const val EXTEND_SESSION = "extend_session"
    const val HISTORY = "history"

    // User profile
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val ADD_VEHICLE = "add_vehicle"
    const val PAYMENT_METHODS = "payment_methods"
    const val ADD_PAYMENT = "add_payment"
    const val NOTIFICATIONS = "notifications"

    // Fines
    const val PAY_FINE = "pay_fine"

    // Admin
    const val ADMIN_ZONES = "admin_zones"
    const val ADMIN_ADD_ZONE = "admin_add_zone"
    const val ADMIN_MANAGE_ZONE = "admin_manage_zone/{zoneId}"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_FINES = "admin_fines"
    const val ADMIN_ISSUE_FINE = "admin_issue_fine"
    const val ADMIN_ALERTS = "admin_alerts"
    const val ADMIN_ALERT_DETAIL = "admin_alert_detail/{alertId}"

    fun adminManageZone(zoneId: String) = "admin_manage_zone/$zoneId"
    fun adminAlertDetail(alertId: String) = "admin_alert_detail/$alertId"
}
