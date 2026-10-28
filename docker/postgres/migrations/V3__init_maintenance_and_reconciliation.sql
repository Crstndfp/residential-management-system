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
