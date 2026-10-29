package com.residencial.app

import com.residencial.app.data.model.QrPass
import org.junit.Assert.*
import org.junit.Test

class QrPassGeneratorTest {

    @Test
    fun `generateToken should create active pass with uppercase plate and SHA-256 hash`() {
        val pass = QrPass.generateToken(
            visitorName = "María Fernanda López",
            plate = "p-982abc",
            houseNumber = "Casa 101",
            hoursValid = 24
        )

        assertNotNull(pass.id)
        assertEquals("P-982ABC", pass.expectedPlate)
        assertEquals("Casa 101", pass.houseNumber)
        assertEquals(QrPass.PassStatus.ACTIVE, pass.status)
        assertTrue(pass.qrPayload.startsWith("RESIPASS::"))
        assertTrue(pass.qrPayload.contains("::HASH::"))
    }
}
