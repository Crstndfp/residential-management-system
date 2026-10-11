package com.residencial.identity.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "casas", schema = "identity_schema")
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "numero_identificador", nullable = false, length = 20, unique = true)
    private String numeroIdentificador;

    @Column(name = "manzana_o_calle", nullable = false, length = 30)
    private String manzanaOCalle;

    @Column(name = "coeficiente_copropiedad", precision = 5, scale = 2)
    private BigDecimal coeficienteCopropiedad;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Casa() {
        // Para JPA
    }

    public Casa(String numeroIdentificador, String manzanaOCalle, BigDecimal coeficienteCopropiedad) {
        this.numeroIdentificador = numeroIdentificador;
        this.manzanaOCalle = manzanaOCalle;
        this.coeficienteCopropiedad = coeficienteCopropiedad != null ? coeficienteCopropiedad : BigDecimal.ONE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNumeroIdentificador() {
        return numeroIdentificador;
    }

    public String getManzanaOCalle() {
        return manzanaOCalle;
    }

    public BigDecimal getCoeficienteCopropiedad() {
        return coeficienteCopropiedad;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
