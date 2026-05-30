package com.example.myapplication.data.repository

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

private fun colones(value: Double): String = "₡${value.toLong()}"

fun ZoneDto.toDomain(): ParkingZone = ParkingZone(
    id = id.toString(),
    municipalityId = municipalityId,
    municipalityName = municipalityName,
    name = name,
    rate = "${colones(hourlyRate)}/hr",
    hours = "6:00 am - 24:00 pm",
    totalSpots = totalSpots,
    freeSpots = freeSpots,
    isActive = isActive,
)

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

fun FineDto.toDomain(): Fine = Fine(
    id = id.toString(),
    zoneName = zoneName,
    spaceNumber = "",
    plate = plate,
    date = issuedAt.take(10),
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
