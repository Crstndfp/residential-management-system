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
