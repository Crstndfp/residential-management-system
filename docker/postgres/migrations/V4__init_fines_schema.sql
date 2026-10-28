-- =============================================================================
-- Migration V4: Fine Types Catalog and Applied Fines
-- =============================================================================

-- Table: fine_types (Configurable infractions catalog)
CREATE TABLE IF NOT EXISTS finance_schema.fine_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    default_amount NUMERIC(12, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fine_types_code UNIQUE (code),
    CONSTRAINT chk_fine_types_amount_positive CHECK (default_amount > 0)
);

-- Table: fines (Applied penalties to specific houses)
CREATE TABLE IF NOT EXISTS finance_schema.fines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    fine_type_id UUID NOT NULL REFERENCES finance_schema.fine_types(id) ON DELETE RESTRICT,
    amount NUMERIC(12, 2) NOT NULL,
    reason TEXT NOT NULL,
    evidence_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_by_user_id UUID,
    paid_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_fines_amount_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_fines_house_id ON finance_schema.fines(house_id);
CREATE INDEX IF NOT EXISTS idx_fines_house_number ON finance_schema.fines(house_number);
CREATE INDEX IF NOT EXISTS idx_fines_status ON finance_schema.fines(status);

-- Seed Essential Fine Types
INSERT INTO finance_schema.fine_types (code, name, description, default_amount) VALUES
    ('NOISE_VIOLATION', 'Ruidos Excesivos Fuera de Horario', 'Música o ruidos molestos pasadas las 22:00 horas', 300.00),
    ('SPEEDING', 'Exceso de Velocidad', 'Conducir a más de 20 km/h en calles residenciales', 500.00),
    ('TRASH_DISPOSAL', 'Basura en Áreas Comunes', 'Desechos depositados en vías públicas o fuera de horario', 250.00),
    ('PET_LEASH', 'Mascotas sin Correa / Desechos', 'Mascotas sueltas sin correa o no recoger sus necesidades', 200.00),
    ('PARKING_ABUSE', 'Uso Indebido de Parqueo Común', 'Pernocta reiterada de vehículos de residentes en parqueo de visitas', 400.00)
ON CONFLICT (code) DO NOTHING;
