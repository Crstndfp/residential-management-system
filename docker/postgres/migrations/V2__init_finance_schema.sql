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
