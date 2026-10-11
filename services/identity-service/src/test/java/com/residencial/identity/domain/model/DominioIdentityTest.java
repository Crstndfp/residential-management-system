package com.residencial.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DominioIdentityTest {

    @Test
    @DisplayName("Invariante: Un propietario asignado a una casa puede ostentar cargo en Junta Directiva")
    void testPropietarioPuedeTenerCargoEnJuntaDirectiva() {
        Casa casa = new Casa("M-01 L-01", "Calle Las Flores", new BigDecimal("1.00"));
        Usuario propietario = new Usuario(casa, "carlos@example.com", "Carlos Flores", RolResidencial.PROPIETARIO);

        assertDoesNotThrow(() -> propietario.asignarCargoJuntaDirectiva(CargoJuntaDirectiva.PRESIDENTE));
        assertEquals(CargoJuntaDirectiva.PRESIDENTE, propietario.getCargoJuntaDirectiva());
    }

    @Test
    @DisplayName("Invariante: Un inquilino NO puede ostentar cargo en la Junta Directiva")
    void testInquilinoNoPuedeTenerCargoEnJuntaDirectiva() {
        Casa casa = new Casa("M-01 L-02", "Calle Las Flores", new BigDecimal("1.00"));
        Usuario inquilino = new Usuario(casa, "inquilino@example.com", "Juan Perez", RolResidencial.INQUILINO);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> inquilino.asignarCargoJuntaDirectiva(CargoJuntaDirectiva.TESORERO)
        );

        assertTrue(exception.getMessage().contains("Solo un propietario"));
    }
}
