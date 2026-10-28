-- =============================================================================
-- Migration V6: Amenities Configuration and Bookings Schema
-- =============================================================================

CREATE TABLE IF NOT EXISTS amenities_schema.amenities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    rental_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    deposit_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    opening_time TIME NOT NULL DEFAULT '08:00:00',
    closing_time TIME NOT NULL DEFAULT '22:00:00',
    allowed_event_types VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_amenity_code UNIQUE (code),
    CONSTRAINT chk_amenity_capacity CHECK (capacity > 0),
    CONSTRAINT chk_amenity_rental_cost CHECK (rental_cost >= 0),
    CONSTRAINT chk_amenity_deposit_cost CHECK (deposit_cost >= 0)
);

CREATE INDEX IF NOT EXISTS idx_amenities_code ON amenities_schema.amenities(code);
CREATE INDEX IF NOT EXISTS idx_amenities_is_active ON amenities_schema.amenities(is_active);

CREATE TABLE IF NOT EXISTS amenities_schema.bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    amenity_id UUID NOT NULL REFERENCES amenities_schema.amenities(id) ON DELETE RESTRICT,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    resident_user_id UUID NOT NULL,
    resident_name VARCHAR(150) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    total_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    deposit_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    finance_charge_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_booking_time_order CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_bookings_amenity_date ON amenities_schema.bookings(amenity_id, booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_house_id ON amenities_schema.bookings(house_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON amenities_schema.bookings(status);

-- Seed Initial Residential Amenities
INSERT INTO amenities_schema.amenities (code, name, description, capacity, rental_cost, deposit_cost, opening_time, closing_time, allowed_event_types)
VALUES 
('CLUB_HOUSE', 'Casa Club Principal', 'Salón de eventos cerrado con aire acondicionado, cocina y baños', 120, 500.00, 300.00, '08:00:00', '23:00:00', 'BIRTHDAY,WEDDING,FAMILY_GATHERING,COMMUNITY_MEETING'),
('GAZEBO', 'Kiosco con Pérgola y Churrasquera', 'Área techada al aire libre con parrilla y mesas campestres', 40, 150.00, 100.00, '09:00:00', '20:00:00', 'BARBECUE,BIRTHDAY,FAMILY_GATHERING'),
('SOCCER_COURT', 'Cancha Sintética de Futsal', 'Cancha iluminada con graderíos', 20, 75.00, 50.00, '06:00:00', '22:00:00', 'SPORTS_MATCH,TOURNAMENT')
ON CONFLICT (code) DO NOTHING;
