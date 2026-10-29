package com.residencial.app.data.repository

import com.residencial.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ResidentRepository {

    private val _resident = MutableStateFlow(ResidentProfile())
    val resident: StateFlow<ResidentProfile> = _resident.asStateFlow()

    private val _passes = MutableStateFlow(
        listOf(
            QrPass.generateToken("Ana Sofía Gómez", "P-881BCD", "Casa 101", 18),
            QrPass.generateToken("Felipe Estrada", "P-224XYZ", "Casa 101", 12)
        )
    )
    val passes: StateFlow<List<QrPass>> = _passes.asStateFlow()

    private val _amenities = MutableStateFlow(
        listOf(
            MobileAmenity("am-1", "SALON_SOCIAL", "Salón Social Principal", "Salón con A/C y cocina equipada", 80, 350.0, 500.0, "09:00 - 23:00"),
            MobileAmenity("am-2", "CHURRASQUERAS", "Pérgola & Churrasqueras", "Área de asadores al aire libre", 30, 150.0, 200.0, "10:00 - 20:00"),
            MobileAmenity("am-3", "CANCHA_SINTETICA", "Cancha Polideportiva", "Fútbol 5 y básquetbol con iluminación", 20, 75.0, 100.0, "06:00 - 22:00")
        )
    )
    val amenities: StateFlow<List<MobileAmenity>> = _amenities.asStateFlow()

    private val _myBookings = MutableStateFlow(
        listOf(
            MobileBooking(
                amenityName = "Salón Social Principal",
                date = "2026-11-05",
                timeSlot = "14:00 - 18:00",
                totalCost = 350.0
            )
        )
    )
    val myBookings: StateFlow<List<MobileBooking>> = _myBookings.asStateFlow()

    private val _tickets = MutableStateFlow(
        listOf(
            MobileTicket(
                folio = "MNT-2026-001",
                title = "Fuga de agua en área verde",
                category = "PLUMBING",
                priority = "HIGH",
                status = "IN_PROGRESS",
                description = "Acumulación de agua frente a lote 105.",
                createdAt = "2026-10-27"
            )
        )
    )
    val tickets: StateFlow<List<MobileTicket>> = _tickets.asStateFlow()

    private val _motions = MutableStateFlow(
        listOf(
            MobileAssemblyMotion(
                id = "mot-001",
                title = "Aprobación Presupuesto Operativo 2026",
                description = "Incremento del 5% para fondo de reserva y cámaras.",
                status = "OPEN"
            ),
            MobileAssemblyMotion(
                id = "mot-002",
                title = "Instalación de Cámaras LPR en Garita",
                description = "Lectura automatizada de placas vehiculares.",
                status = "PENDING"
            )
        )
    )
    val motions: StateFlow<List<MobileAssemblyMotion>> = _motions.asStateFlow()

    private val _quorumPercentage = MutableStateFlow(52.0)
    val quorumPercentage: StateFlow<Double> = _quorumPercentage.asStateFlow()

    private val _panicAlerts = MutableStateFlow<List<PanicAlert>>(emptyList())
    val panicAlerts: StateFlow<List<PanicAlert>> = _panicAlerts.asStateFlow()

    // Pass actions
    fun createVisitorPass(visitorName: String, plate: String, hoursValid: Long = 24): QrPass {
        val newPass = QrPass.generateToken(
            visitorName = visitorName,
            plate = plate,
            houseNumber = _resident.value.houseNumber,
            hoursValid = hoursValid
        )
        _passes.value = listOf(newPass) + _passes.value
        return newPass
    }

    // Booking actions
    fun createBooking(amenity: MobileAmenity, date: String, slot: String) {
        val newBooking = MobileBooking(
            amenityName = amenity.name,
            date = date,
            timeSlot = slot,
            totalCost = amenity.rentalCost
        )
        _myBookings.value = listOf(newBooking) + _myBookings.value
    }

    // Ticket actions
    fun createTicket(title: String, category: String, description: String, priority: String) {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val newTicket = MobileTicket(
            folio = "MNT-2026-${String.format("%03d", _tickets.value.size + 1)}",
            title = title,
            category = category,
            priority = priority,
            status = "OPEN",
            description = description,
            createdAt = LocalDateTime.now().format(dtf)
        )
        _tickets.value = listOf(newTicket) + _tickets.value
    }

    // Assembly voting action with SHA-256 seal
    fun castVote(motionId: String, choice: String): String {
        val voterId = _resident.value.id
        val raw = "$motionId|$voterId|$choice|${System.currentTimeMillis()}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

        _motions.value = _motions.value.map { m ->
            if (m.id == motionId) {
                m.copy(userVotedChoice = choice)
            } else m
        }
        return hash
    }

    // Emergency Panic Trigger
    fun triggerPanicButton(): PanicAlert {
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val alert = PanicAlert(
            houseNumber = _resident.value.houseNumber,
            residentName = _resident.value.fullName,
            timestamp = LocalDateTime.now().format(dtf)
        )
        _panicAlerts.value = listOf(alert) + _panicAlerts.value
        return alert
    }
}
