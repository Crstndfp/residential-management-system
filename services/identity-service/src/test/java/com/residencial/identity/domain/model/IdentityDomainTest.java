package com.residencial.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IdentityDomainTest {

    @Test
    @DisplayName("Domain Invariant: An owner assigned to a house can hold a Board of Directors position")
    void testOwnerCanHoldBoardRole() {
        House house = new House("H-101", "Palm Street", new BigDecimal("1.00"));
        User owner = new User(house, "carlos@example.com", "Carlos Flores", ResidentialRole.OWNER);

        assertDoesNotThrow(() -> owner.assignBoardRole(BoardRole.PRESIDENT));
        assertEquals(BoardRole.PRESIDENT, owner.getBoardRole());
    }

    @Test
    @DisplayName("Domain Invariant: A tenant cannot hold a Board of Directors position")
    void testTenantCannotHoldBoardRole() {
        House house = new House("H-102", "Palm Street", new BigDecimal("1.00"));
        User tenant = new User(house, "tenant@example.com", "John Doe", ResidentialRole.TENANT);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tenant.assignBoardRole(BoardRole.TREASURER)
        );

        assertTrue(exception.getMessage().contains("Only an owner"));
    }
}
