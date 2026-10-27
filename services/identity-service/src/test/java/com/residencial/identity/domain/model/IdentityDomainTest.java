package com.residencial.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityDomainTest {

    // Valid Guatemalan DPI sample: 2548963210101
    // Calculation:
    // 2*2 + 5*3 + 4*4 + 8*5 + 9*6 + 6*7 + 3*8 + 2*9 = 4 + 15 + 16 + 40 + 54 + 42 + 24 + 18 = 213
    // 213 % 11 = 4 -> (11 - 4) % 11 = 7.
    // Let's craft exact valid digits:
    // Digits: 1 2 3 4 5 6 7 8
    // Sum = 1*2 + 2*3 + 3*4 + 4*5 + 5*6 + 6*7 + 7*8 + 8*9
    // Sum = 2 + 6 + 12 + 20 + 30 + 42 + 56 + 72 = 240
    // 240 % 11 = 9 -> (11 - 9) % 11 = 2 (verifier = 2).
    // Dept = 01 (Guatemala), Mun = 01. Full CUI: "1234567820101"
    private static final String VALID_DPI = "1234567820101";

    @Test
    @DisplayName("Domain Invariant: An owner assigned to a house can hold a Board of Directors position")
    void testOwnerCanHoldBoardRole() {
        House house = new House("H-101", "Palm Street");
        IdentificationDocument doc = new IdentificationDocument(IdentificationType.DPI, VALID_DPI);
        User owner = new User(house, doc, "carlos@example.com", "Carlos Flores", ResidentialRole.OWNER);

        assertDoesNotThrow(() -> owner.assignBoardRole(BoardRole.PRESIDENT));
        assertEquals(BoardRole.PRESIDENT, owner.getBoardRole());
    }

    @Test
    @DisplayName("Domain Invariant: A tenant cannot hold a Board of Directors position")
    void testTenantCannotHoldBoardRole() {
        House house = new House("H-102", "Palm Street");
        IdentificationDocument doc = new IdentificationDocument(IdentificationType.PASSPORT, "A12345678");
        User tenant = new User(house, doc, "tenant@example.com", "John Doe", ResidentialRole.TENANT);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tenant.assignBoardRole(BoardRole.TREASURER)
        );

        assertTrue(exception.getMessage().contains("Only an owner"));
    }

    @Test
    @DisplayName("Identification: Valid Guatemalan DPI with Modulo 11 passes")
    void testValidDpiVerification() {
        IdentificationDocument doc = new IdentificationDocument(IdentificationType.DPI, "1234 5678 2 0101");
        assertEquals("1234567820101", doc.getNumber());
        assertEquals(IdentificationType.DPI, doc.getType());
    }

    @Test
    @DisplayName("Identification: Invalid DPI checksum fails with Modulo 11")
    void testInvalidDpiChecksumFails() {
        // Change verifier from 2 to 9
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new IdentificationDocument(IdentificationType.DPI, "1234567890101")
        );
        assertTrue(ex.getMessage().contains("Modulo 11 verification failed"));
    }

    @Test
    @DisplayName("Identification: Invalid department code fails")
    void testInvalidDpiDepartmentFails() {
        // Dept 23 does not exist in Guatemala (01 to 22)
        // Let's use verifier 2 with dept 23
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new IdentificationDocument(IdentificationType.DPI, "1234567822301")
        );
        assertTrue(ex.getMessage().contains("Invalid department code"));
    }

    @Test
    @DisplayName("Identification: Foreign passport format validation")
    void testValidPassportPasses() {
        IdentificationDocument doc = new IdentificationDocument(IdentificationType.PASSPORT, "US889977X");
        assertEquals("US889977X", doc.getNumber());
        assertEquals(IdentificationType.PASSPORT, doc.getType());
    }

    @Test
    @DisplayName("Identification: Invalid passport format throws exception")
    void testInvalidPassportFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IdentificationDocument(IdentificationType.PASSPORT, "A@#$")
        );
    }
}
