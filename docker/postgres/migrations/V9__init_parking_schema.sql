-- =============================================================================
-- Migration V9: Parking, Guard Gate Check-In & License Plate Analytics
-- =============================================================================

-- Table: parking_spots
CREATE TABLE IF NOT EXISTS parking_schema.parking_spots (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    spot_number VARCHAR(20) NOT NULL UNIQUE,
    spot_type VARCHAR(30) NOT NULL DEFAULT 'VISITOR',
    is_occupied BOOLEAN NOT NULL DEFAULT false,
    current_vehicle_entry_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_parking_spots_occupied ON parking_schema.parking_spots(is_occupied);
CREATE INDEX IF NOT EXISTS idx_parking_spots_number ON parking_schema.parking_spots(spot_number);

-- Table: vehicle_entries
CREATE TABLE IF NOT EXISTS parking_schema.vehicle_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate VARCHAR(20) NOT NULL,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    visitor_name VARCHAR(150) NOT NULL,
    visitor_id_document VARCHAR(50) NOT NULL,
    parking_spot_id UUID REFERENCES parking_schema.parking_spots(id),
    spot_number VARCHAR(20) NOT NULL,
    guard_user_id UUID NOT NULL,
    entry_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    exit_time TIMESTAMPTZ,
    duration_minutes BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    is_overnight BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_entries_plate ON parking_schema.vehicle_entries(plate);
CREATE INDEX IF NOT EXISTS idx_entries_house ON parking_schema.vehicle_entries(house_id);
CREATE INDEX IF NOT EXISTS idx_entries_status ON parking_schema.vehicle_entries(status);
CREATE INDEX IF NOT EXISTS idx_entries_entry_time ON parking_schema.vehicle_entries(entry_time);
CREATE INDEX IF NOT EXISTS idx_entries_overnight ON parking_schema.vehicle_entries(is_overnight);

-- Table: parking_fines
CREATE TABLE IF NOT EXISTS parking_schema.parking_fines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate VARCHAR(20) NOT NULL,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    fine_amount NUMERIC(12, 2) NOT NULL DEFAULT 50.00,
    reason VARCHAR(255) NOT NULL,
    evidence_summary TEXT NOT NULL,
    applied_by_user_id UUID NOT NULL,
    applied_by_name VARCHAR(150) NOT NULL,
    finance_charge_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_parking_fines_plate ON parking_schema.parking_fines(plate);
CREATE INDEX IF NOT EXISTS idx_parking_fines_house ON parking_schema.parking_fines(house_id);

-- Seed initial 20 Visitor Parking Spots (P-01 to P-20)
INSERT INTO parking_schema.parking_spots (spot_number, spot_type, is_occupied)
VALUES
    ('P-01', 'VISITOR', false),
    ('P-02', 'VISITOR', false),
    ('P-03', 'VISITOR', false),
    ('P-04', 'VISITOR', false),
    ('P-05', 'VISITOR', false),
    ('P-06', 'VISITOR', false),
    ('P-07', 'VISITOR', false),
    ('P-08', 'VISITOR', false),
    ('P-09', 'VISITOR', false),
    ('P-10', 'VISITOR', false),
    ('P-11', 'VISITOR', false),
    ('P-12', 'VISITOR', false),
    ('P-13', 'VISITOR', false),
    ('P-14', 'VISITOR', false),
    ('P-15', 'VISITOR', false),
    ('P-16', 'VISITOR', false),
    ('P-17', 'VISITOR', false),
    ('P-18', 'VISITOR', false),
    ('P-19', 'VISITOR', false),
    ('P-20', 'VISITOR', false)
ON CONFLICT (spot_number) DO NOTHING;
