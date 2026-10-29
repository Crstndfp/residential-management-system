-- Habilitar extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Esquemas aislados por microservicio / bounded context
CREATE SCHEMA IF NOT EXISTS identity_schema;
CREATE SCHEMA IF NOT EXISTS finance_schema;
CREATE SCHEMA IF NOT EXISTS amenities_schema;
CREATE SCHEMA IF NOT EXISTS parking_schema;
-- =============================================================================
-- Migration V1: Initial Schemas and Extensions
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Isolated Bounded Context Schemas
CREATE SCHEMA IF NOT EXISTS identity_schema;
CREATE SCHEMA IF NOT EXISTS finance_schema;
CREATE SCHEMA IF NOT EXISTS amenities_schema;
CREATE SCHEMA IF NOT EXISTS parking_schema;

-- =============================================================================
-- IDENTITY_SCHEMA: Tables
-- =============================================================================

-- Table: houses
CREATE TABLE IF NOT EXISTS identity_schema.houses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_number VARCHAR(20) NOT NULL,
    street_or_block VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_houses_house_number UNIQUE (house_number)
);

-- Table: users
CREATE TABLE IF NOT EXISTS identity_schema.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_id UUID REFERENCES identity_schema.houses(id) ON DELETE SET NULL,
    id_type VARCHAR(20) NOT NULL,
    id_number VARCHAR(30) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    residential_role VARCHAR(30) NOT NULL,
    board_role VARCHAR(30) NOT NULL DEFAULT 'NONE',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_id_number UNIQUE (id_number)
);

-- Invariant Enforcement: Only 1 active OWNER per house
CREATE UNIQUE INDEX IF NOT EXISTS uq_house_single_owner 
    ON identity_schema.users(house_id) 
    WHERE residential_role = 'OWNER';

-- Invariant Enforcement: Only 1 active TENANT per house
CREATE UNIQUE INDEX IF NOT EXISTS uq_house_single_tenant 
    ON identity_schema.users(house_id) 
    WHERE residential_role = 'TENANT';

-- Table: invitation_tokens
CREATE TABLE IF NOT EXISTS identity_schema.invitation_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES identity_schema.users(id) ON DELETE CASCADE,
    token VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_invitation_tokens_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_invitation_tokens_user_id ON identity_schema.invitation_tokens(user_id);
-- =============================================================================
-- Migration V2: Finance Schema - Expense Categories, Vendors and Invoices
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS finance_schema;

-- Table: expense_categories
CREATE TABLE IF NOT EXISTS finance_schema.expense_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_expense_categories_code UNIQUE (code)
);

-- Table: vendors
CREATE TABLE IF NOT EXISTS finance_schema.vendors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tax_id VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(30),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vendors_tax_id UNIQUE (tax_id)
);

-- Table: invoices
CREATE TABLE IF NOT EXISTS finance_schema.invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number VARCHAR(50) NOT NULL,
    vendor_id UUID NOT NULL REFERENCES finance_schema.vendors(id) ON DELETE RESTRICT,
    category_id UUID NOT NULL REFERENCES finance_schema.expense_categories(id) ON DELETE RESTRICT,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    payment_date DATE,
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    document_url VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_invoices_vendor_number UNIQUE (vendor_id, invoice_number),
    CONSTRAINT chk_invoices_amount_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_invoices_vendor_id ON finance_schema.invoices(vendor_id);
CREATE INDEX IF NOT EXISTS idx_invoices_category_id ON finance_schema.invoices(category_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON finance_schema.invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_issue_date ON finance_schema.invoices(issue_date);

-- Seed Initial Essential Categories for Residential Complexes
INSERT INTO finance_schema.expense_categories (code, name, description) VALUES
    ('SECURITY', 'Seguridad Privada y Garita', 'Servicios de vigilancia 24/7 y monitoreo'),
    ('ELECTRICITY', 'Energía Eléctrica Común', 'Alumbrado público, garita y bombas'),
    ('WATER', 'Agua Potable y Cisterna', 'Suministro y mantenimiento de bombas de agua'),
    ('GARDENING', 'Jardinería y Áreas Verdes', 'Mantenimiento de parques, podas y áreas comunes'),
    ('CLEANING', 'Limpieza y Desechos', 'Extracción de basura y limpieza de vías comunes'),
    ('MAINTENANCE', 'Mantenimiento Preventivo y Reparaciones', 'Reparaciones mecánicas, portones y pintura')
ON CONFLICT (code) DO NOTHING;
-- =============================================================================
-- Migration V3: Maintenance Charges and Bank Reconciliation Transactions
-- =============================================================================

-- Table: maintenance_charges (Monthly charges billed to houses)
CREATE TABLE IF NOT EXISTS finance_schema.maintenance_charges (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    paid_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_charge_house_period UNIQUE (house_id, period_year, period_month),
    CONSTRAINT chk_charge_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_charge_paid_non_negative CHECK (paid_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_charges_house_id ON finance_schema.maintenance_charges(house_id);
CREATE INDEX IF NOT EXISTS idx_charges_house_number ON finance_schema.maintenance_charges(house_number);
CREATE INDEX IF NOT EXISTS idx_charges_period ON finance_schema.maintenance_charges(period_year, period_month);

-- Table: bank_transactions (Parsed and reconciled transactions from bank reports)
CREATE TABLE IF NOT EXISTS finance_schema.bank_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    authorization_number VARCHAR(50) NOT NULL,
    house_reference VARCHAR(50) NOT NULL,
    matched_charge_id UUID REFERENCES finance_schema.maintenance_charges(id) ON DELETE SET NULL,
    transaction_date DATE NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    source_file_name VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'RECONCILED',
    discrepancy_reason VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bank_authorization UNIQUE (authorization_number),
    CONSTRAINT chk_bank_amount_positive CHECK (amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_bank_tx_authorization ON finance_schema.bank_transactions(authorization_number);
CREATE INDEX IF NOT EXISTS idx_bank_tx_house_reference ON finance_schema.bank_transactions(house_reference);
CREATE INDEX IF NOT EXISTS idx_bank_tx_status ON finance_schema.bank_transactions(status);
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
-- =============================================================================
-- Migration V5: Fixed Assets & Equipment Inventory (Patrimonio y Mobiliario)
-- =============================================================================

-- Table: fixed_assets
CREATE TABLE IF NOT EXISTS finance_schema.fixed_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    physical_location VARCHAR(150) NOT NULL,
    acquisition_cost NUMERIC(12, 2) NOT NULL,
    acquisition_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EXCELLENT',
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_fixed_asset_code UNIQUE (code),
    CONSTRAINT chk_asset_cost_positive CHECK (acquisition_cost > 0)
);

CREATE INDEX IF NOT EXISTS idx_assets_code ON finance_schema.fixed_assets(code);
CREATE INDEX IF NOT EXISTS idx_assets_category ON finance_schema.fixed_assets(category);
CREATE INDEX IF NOT EXISTS idx_assets_status ON finance_schema.fixed_assets(status);

-- Table: asset_status_history (Audit log of physical condition changes)
CREATE TABLE IF NOT EXISTS finance_schema.asset_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    asset_id UUID NOT NULL REFERENCES finance_schema.fixed_assets(id) ON DELETE CASCADE,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    reason TEXT NOT NULL,
    changed_by_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_asset_history_asset_id ON finance_schema.asset_status_history(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_history_created_at ON finance_schema.asset_status_history(created_at);
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
-- =============================================================================
-- Migration V7: Governance, Assemblies, Digital Roll Call and Secure Voting
-- =============================================================================

-- Table: assemblies
CREATE TABLE IF NOT EXISTS amenities_schema.assemblies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(150) NOT NULL,
    assembly_type VARCHAR(30) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    agenda TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONVOKED',
    total_residential_houses INT NOT NULL DEFAULT 100,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_assembly_houses_pos CHECK (total_residential_houses > 0)
);

CREATE INDEX IF NOT EXISTS idx_assemblies_status ON amenities_schema.assemblies(status);
CREATE INDEX IF NOT EXISTS idx_assemblies_scheduled ON amenities_schema.assemblies(scheduled_at);

-- Table: assembly_attendance (Digital Roll Call)
CREATE TABLE IF NOT EXISTS amenities_schema.assembly_attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assembly_id UUID NOT NULL REFERENCES amenities_schema.assemblies(id) ON DELETE CASCADE,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    attended_by_user_id UUID NOT NULL,
    attended_by_name VARCHAR(150) NOT NULL,
    is_present BOOLEAN NOT NULL DEFAULT true,
    checked_in_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_assembly_house UNIQUE (assembly_id, house_id)
);

CREATE INDEX IF NOT EXISTS idx_attendance_assembly ON amenities_schema.assembly_attendance(assembly_id);
CREATE INDEX IF NOT EXISTS idx_attendance_house ON amenities_schema.assembly_attendance(house_id);

-- Table: assembly_motions
CREATE TABLE IF NOT EXISTS amenities_schema.assembly_motions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assembly_id UUID NOT NULL REFERENCES amenities_schema.assemblies(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verdict VARCHAR(30),
    opened_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_motions_assembly ON amenities_schema.assembly_motions(assembly_id);
CREATE INDEX IF NOT EXISTS idx_motions_status ON amenities_schema.assembly_motions(status);

-- Table: motion_votes (1 Vote per House Invariant + Cryptographic Audit Hash)
CREATE TABLE IF NOT EXISTS amenities_schema.motion_votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    motion_id UUID NOT NULL REFERENCES amenities_schema.assembly_motions(id) ON DELETE CASCADE,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    voter_user_id UUID NOT NULL,
    vote_choice VARCHAR(20) NOT NULL,
    vote_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_motion_house_vote UNIQUE (motion_id, house_id)
);

CREATE INDEX IF NOT EXISTS idx_votes_motion ON amenities_schema.motion_votes(motion_id);
CREATE INDEX IF NOT EXISTS idx_votes_house ON amenities_schema.motion_votes(house_id);
-- =============================================================================
-- Migration V8: Maintenance Tickets & Incident Tracking (US-501 & US-502)
-- =============================================================================

-- Table: maintenance_tickets
CREATE TABLE IF NOT EXISTS amenities_schema.maintenance_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    folio VARCHAR(30) NOT NULL UNIQUE,
    reporter_user_id UUID NOT NULL,
    reporter_name VARCHAR(150) NOT NULL,
    reporter_role VARCHAR(30) NOT NULL,
    house_id UUID NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    photo_url VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    assigned_technician VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_tickets_folio ON amenities_schema.maintenance_tickets(folio);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON amenities_schema.maintenance_tickets(status);
CREATE INDEX IF NOT EXISTS idx_tickets_house ON amenities_schema.maintenance_tickets(house_id);
CREATE INDEX IF NOT EXISTS idx_tickets_created_at ON amenities_schema.maintenance_tickets(created_at);

-- Table: ticket_updates (Audit & Progress History)
CREATE TABLE IF NOT EXISTS amenities_schema.ticket_updates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES amenities_schema.maintenance_tickets(id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL,
    author_name VARCHAR(150) NOT NULL,
    author_role VARCHAR(30) NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    notes TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ticket_updates_ticket ON amenities_schema.ticket_updates(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_updates_created_at ON amenities_schema.ticket_updates(created_at);
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
