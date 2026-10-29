package com.residencial.app.data.model

import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ResidentProfile(
    val id: String = "usr-res-101",
    val fullName: String = "Carlos Méndez Rodríguez",
    val email: String = "carlos.mendez@condominio.gt",
    val houseId: String = "h-101",
    val houseNumber: String = "Casa 101",
    val isSolvent: Boolean = true,
    val pendingBalance: Double = 0.0,
    val monthlyMaintenanceFee: Double = 650.0
)

data class QrPass(
    val id: String = UUID.randomUUID().toString(),
    val visitorName: String,
    val visitorIdDocument: String,
    val expectedPlate: String,
    val houseNumber: String,
    val validUntil: String,
    val qrPayload: String,
    val status: PassStatus = PassStatus.ACTIVE,
    val createdAt: String
) {
    enum class PassStatus {
        ACTIVE, USED, EXPIRED
    }

    companion object {
        fun generateToken(
            visitorName: String,
            plate: String,
            houseNumber: String,
            hoursValid: Long = 24
        ): QrPass {
            val now = LocalDateTime.now()
            val expiry = now.plusHours(hoursValid)
            val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val raw = "PASS|${UUID.randomUUID()}|$houseNumber|$plate|$visitorName|${expiry.format(dtf)}"
            val hash = MessageDigest.getInstance("SHA-256")
                .digest(raw.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .take(32)

            val payload = "RESIPASS::$raw::HASH::$hash"

            return QrPass(
                visitorName = visitorName,
                visitorIdDocument = "DPI Registrado",
                expectedPlate = plate.uppercase().trim(),
                houseNumber = houseNumber,
                validUntil = expiry.format(dtf),
                qrPayload = payload,
                status = PassStatus.ACTIVE,
                createdAt = now.format(dtf)
            )
        }
    }
}

data class MobileAmenity(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val capacity: Int,
    val rentalCost: Double,
    val depositCost: Double,
    val hours: String
)

data class MobileBooking(
    val id: String = UUID.randomUUID().toString(),
    val amenityName: String,
    val date: String,
    val timeSlot: String,
    val totalCost: Double,
    val status: String = "CONFIRMED"
)

data class MobileTicket(
    val id: String = UUID.randomUUID().toString(),
    val folio: String,
    val title: String,
    val category: String,
    val priority: String,
    val status: String,
    val description: String,
    val createdAt: String
)

data class MobileAssemblyMotion(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val userVotedChoice: String? = null
)

data class PanicAlert(
    val alertId: String = UUID.randomUUID().toString(),
    val houseNumber: String,
    val residentName: String,
    val timestamp: String,
    val status: String = "DISPATCHED_TO_GUARD"
)
