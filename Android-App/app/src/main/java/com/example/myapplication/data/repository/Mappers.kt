package com.example.myapplication.data.repository

import com.example.myapplication.data.ActiveSession
import com.example.myapplication.data.AdminReportSummary
import com.example.myapplication.data.Fine
import com.example.myapplication.data.ParkingSession
import com.example.myapplication.data.ParkingZone
import com.example.myapplication.data.PaymentMethod
import com.example.myapplication.data.Vehicle
import com.example.myapplication.data.remote.FineDto
import com.example.myapplication.data.remote.PaymentMethodDto
import com.example.myapplication.data.remote.ReportSummaryDto
import com.example.myapplication.data.remote.SessionDto
import com.example.myapplication.data.remote.VehicleDto
import com.example.myapplication.data.remote.ZoneDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun colones(value: Double): String = "₡${value.toLong()}"

private fun hourLabel(h: Int): String {
    val h24 = if (h == 24) 0 else h
    val period = if (h < 12 || h >= 24) "am" else "pm"
    val h12 = when {
        h24 == 0 -> 12
        h24 <= 12 -> h24
        else -> h24 - 12
    }
    return "$h12:00 $period"
}

fun ZoneDto.toDomain(): ParkingZone = ParkingZone(
    id = id.toString(),
    municipalityId = municipalityId,
    municipalityName = municipalityName,
    name = name,
    rate = "${colones(hourlyRate)}/hr",
    hours = "${hourLabel(openHour)} - ${hourLabel(closeHour)}",
    totalSpots = totalSpots,
    freeSpots = freeSpots,
    openHour = openHour,
    closeHour = closeHour,
    isActive = isActive,
    hourlyRate = hourlyRate,
)

private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun parseIsoMs(iso: String): Long = runCatching {
    isoFormat.parse(iso.trimEnd('Z'))?.time ?: 0L
}.getOrDefault(0L)

fun SessionDto.toDomain(): ParkingSession = ParkingSession(
    id = id.toString(),
    zoneName = zoneName,
    spaceNumber = "#%04d".format(spaceNumber),
    plate = plate,
    date = scheduledStart.take(10),
    duration = "",
    total = colones(totalCost),
    status = when (status) {
        "Active" -> "Activa"
        "Completed" -> "Pagado"
        "Cancelled" -> "Cancelada"
        "Expired" -> "Expirada"
        else -> status
    },
)

fun SessionDto.toActiveSession(): ActiveSession = ActiveSession(
    id = id,
    zoneName = zoneName,
    zoneOpenHour = zoneOpenHour,
    zoneCloseHour = zoneCloseHour,
    plate = plate,
    spaceNumber = spaceNumber,
    scheduledStartMs = parseIsoMs(scheduledStart),
    scheduledEndMs = parseIsoMs(scheduledEnd),
    hourlyRate = hourlyRate,
)

fun FineDto.toDomain(): Fine = Fine(
    id = id.toString(),
    zoneName = zoneName,
    spaceNumber = spaceNumber?.let { "#%04d".format(it) } ?: "—",
    plate = plate,
    date = issuedAt.take(10),
    time = issuedAt.drop(11).take(5),
    amount = colones(amount),
    isPaid = status == "Paid",
)

fun PaymentMethodDto.toDomain(): PaymentMethod = PaymentMethod(
    id = id.toString(),
    label = cardBrand ?: "Tarjeta",
    last4 = lastFour,
    expiry = "%02d/%02d".format(expiryMonth, expiryYear % 100),
    isDefault = isDefault,
)

fun VehicleDto.toDomain(): Vehicle = Vehicle(
    id = id,
    plate = plate,
    brand = brand ?: "",
    model = model ?: "",
    year = "",
)

fun ReportSummaryDto.toDomain(): AdminReportSummary = AdminReportSummary(
    totalSessions = totalSessions,
    revenue = colones(revenue),
    finesIssued = finesIssued,
    activeSpots = "$activeSpots/$totalSpots",
    date = "",
)
