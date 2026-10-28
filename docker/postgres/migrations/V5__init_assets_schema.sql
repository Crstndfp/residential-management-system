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
