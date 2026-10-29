package com.residencial.app

import com.residencial.app.data.repository.ResidentRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ResidentRepositoryTest {

    private lateinit var repository: ResidentRepository

    @Before
    fun setUp() {
        repository = ResidentRepository()
    }

    @Test
    fun `initial resident profile should be solvent with zero pending balance`() {
        val resident = repository.resident.value
        assertEquals("Casa 101", resident.houseNumber)
        assertTrue(resident.isSolvent)
        assertEquals(0.0, resident.pendingBalance, 0.001)
    }

    @Test
    fun `createVisitorPass should add new pass at top of list`() {
        val initialCount = repository.passes.value.size
        val pass = repository.createVisitorPass("Rodrigo Paz", "P-771QWE")

        assertEquals(initialCount + 1, repository.passes.value.size)
        assertEquals("P-771QWE", pass.expectedPlate)
        assertEquals(pass.id, repository.passes.value.first().id)
    }

    @Test
    fun `castVote should update motion choice and return SHA-256 seal`() {
        val hash = repository.castVote("mot-001", "A_FAVOR")

        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 hex string is 64 chars
        val motion = repository.motions.value.find { it.id == "mot-001" }
        assertEquals("A_FAVOR", motion?.userVotedChoice)
    }

    @Test
    fun `triggerPanicButton should create urgent alert for resident house`() {
        val alert = repository.triggerPanicButton()

        assertNotNull(alert.alertId)
        assertEquals("Casa 101", alert.houseNumber)
        assertEquals("DISPATCHED_TO_GUARD", alert.status)
        assertEquals(1, repository.panicAlerts.value.size)
    }

    @Test
    fun `createBooking should append new reservation`() {
        val initialCount = repository.myBookings.value.size
        val amenity = repository.amenities.value.first()
        repository.createBooking(amenity, "2026-11-20", "10:00 - 14:00")

        assertEquals(initialCount + 1, repository.myBookings.value.size)
        assertEquals(amenity.name, repository.myBookings.value.first().amenityName)
    }
}
