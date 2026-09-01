-- Purpose: Database schema for collateral
-- File: collateral-service/src/main/resources/db/migration/V1__create_collateral_tables.sql

--
-- TABLE: collateral_assets
-- Stores all collateral assets
--
CREATE TABLE collateral_assets (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    collateral_reference VARCHAR(30) NOT NULL UNIQUE,
    customer_number VARCHAR(20) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_description NVARCHAR(MAX),
    valuation DECIMAL(19, 2) NOT NULL,
    valuation_date DATE NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(20) DEFAULT 'PENDING',
    loan_number VARCHAR(30),
    pledge_date DATE,
    release_date DATE,
    legal_status VARCHAR(20) DEFAULT 'PENDING',
    insurance_status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);

-- Indexes
CREATE INDEX idx_collateral_assets_customer_number ON collateral_assets(customer_number);
CREATE INDEX idx_collateral_assets_collateral_reference ON collateral_assets(collateral_reference);
CREATE INDEX idx_collateral_assets_status ON collateral_assets(status);
CREATE INDEX idx_collateral_assets_loan_number ON collateral_assets(loan_number);
CREATE INDEX idx_collateral_assets_asset_type ON collateral_assets(asset_type);

--
-- TABLE: collateral_valuations
-- Tracks historical valuations
--
CREATE TABLE collateral_valuations (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    collateral_id BIGINT NOT NULL,
    valuation_date DATE NOT NULL,
    valuation DECIMAL(19, 2) NOT NULL,
    valuation_type VARCHAR(50),
    appraiser VARCHAR(100),
    notes NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_collateral_valuation_collateral FOREIGN KEY (collateral_id)
    REFERENCES collateral_assets(id) ON DELETE CASCADE
);

CREATE INDEX idx_collateral_valuations_collateral_id ON collateral_valuations(collateral_id);
CREATE INDEX idx_collateral_valuations_valuation_date ON collateral_valuations(valuation_date);

--
-- TABLE: collateral_audit
-- Audit trail for collateral changes
--
CREATE TABLE collateral_audit (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    collateral_reference VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    field_name VARCHAR(100),
    old_value NVARCHAR(MAX),
    new_value NVARCHAR(MAX),
    changed_by VARCHAR(100),
    changed_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_collateral_audit_collateral_reference ON collateral_audit(collateral_reference);
CREATE INDEX idx_collateral_audit_changed_at ON collateral_audit(changed_at);

--
-- Initial Data
--
INSERT INTO collateral_assets (
    collateral_reference, customer_number, asset_type, asset_name,
    asset_description, valuation, valuation_date, status, legal_status, insurance_status
)
SELECT 'COL-000001', 'CUST-000001', 'REAL_ESTATE', 'Family Home',
       '123 Main Street, New York, NY 10001', 500000.00, '2026-01-15',
       'ACTIVE', 'VERIFIED', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM collateral_assets WHERE collateral_reference = 'COL-000001');

INSERT INTO collateral_assets (
    collateral_reference, customer_number, asset_type, asset_name,
    asset_description, valuation, valuation_date, status, legal_status, insurance_status
)
SELECT 'COL-000002', 'CUST-000002', 'VEHICLE', '2023 BMW X5',
       'VIN: WBAXXXXXX, Silver', 65000.00, '2026-02-01',
       'ACTIVE', 'VERIFIED', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM collateral_assets WHERE collateral_reference = 'COL-000002');

-- Link some collateral to loans
UPDATE collateral_assets SET loan_number = 'LN-000001', status = 'PLEDGED',
                             pledge_date = '2026-01-20'
WHERE collateral_reference = 'COL-000001';
