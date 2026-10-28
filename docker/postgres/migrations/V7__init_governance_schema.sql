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
