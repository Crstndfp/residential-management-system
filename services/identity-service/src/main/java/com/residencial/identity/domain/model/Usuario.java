package com.residencial.identity.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "identity_schema")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = true)
    private Casa casa;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_residencial", nullable = false, length = 30)
    private RolResidencial rolResidencial;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_junta_directiva", nullable = false, length = 30)
    private CargoJuntaDirectiva cargoJuntaDirectiva;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Usuario() {
        // Para JPA
    }

    public Usuario(Casa casa, String email, String nombreCompleto, RolResidencial rolResidencial) {
        this.casa = casa;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rolResidencial = rolResidencial;
        this.cargoJuntaDirectiva = CargoJuntaDirectiva.NINGUNO;
        this.activo = true;
        this.createdAt = Instant.now();
    }

    public void asignarCargoJuntaDirectiva(CargoJuntaDirectiva cargo) {
        if (this.rolResidencial != RolResidencial.PROPIETARIO && cargo != CargoJuntaDirectiva.NINGUNO) {
            throw new IllegalStateException("Solo un propietario asignado a una vivienda puede ostentar un cargo en la Junta Directiva.");
        }
        this.cargoJuntaDirectiva = cargo;
    }

    public UUID getId() {
        return id;
    }

    public Casa getCasa() {
        return casa;
    }

    public String getEmail() {
        return email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public RolResidencial getRolResidencial() {
        return rolResidencial;
    }

    public CargoJuntaDirectiva getCargoJuntaDirectiva() {
        return cargoJuntaDirectiva;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
