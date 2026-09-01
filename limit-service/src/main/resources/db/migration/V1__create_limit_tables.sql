-- Purpose: Database schema for credit limits
-- File: limit-service/src/main/resources/db/migration/V1__create_limit_tables.sql

--
-- TABLE: customer_credit_limits
-- Stores the overall credit limit for each customer
--
CREATE TABLE customer_credit_limits (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    customer_number VARCHAR(20) NOT NULL UNIQUE,
    total_limit DECIMAL(19, 2) NOT NULL,
    used_limit DECIMAL(19, 2) DEFAULT 0,
    available_limit DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    risk_score INTEGER DEFAULT 50,
    credit_rating VARCHAR(20),
    last_updated DATETIME2 DEFAULT GETDATE(),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0,

    CONSTRAINT chk_total_limit CHECK (total_limit >= 0),
    CONSTRAINT chk_used_limit CHECK (used_limit >= 0),
    CONSTRAINT chk_available_limit CHECK (available_limit >= 0)
);

-- Indexes
CREATE INDEX idx_customer_credit_limits_customer_number ON customer_credit_limits(customer_number);
CREATE INDEX idx_customer_credit_limits_credit_rating ON customer_credit_limits(credit_rating);

--
-- TABLE: product_credit_limits
-- Stores limits per product type
--
CREATE TABLE product_credit_limits (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    customer_limit_id BIGINT NOT NULL,
    product_type VARCHAR(50) NOT NULL,
    limit_amount DECIMAL(19, 2) NOT NULL,
    used_amount DECIMAL(19, 2) DEFAULT 0,
    available_amount DECIMAL(19, 2) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_product_limit_customer FOREIGN KEY (customer_limit_id)
    REFERENCES customer_credit_limits(id) ON DELETE CASCADE,
    CONSTRAINT uk_customer_product UNIQUE (customer_limit_id, product_type)
);

CREATE INDEX idx_product_credit_limits_customer_limit_id ON product_credit_limits(customer_limit_id);
CREATE INDEX idx_product_credit_limits_product_type ON product_credit_limits(product_type);

--
-- TABLE: limit_reservations
-- Tracks temporary reservations during loan applications
--
CREATE TABLE limit_reservations (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    customer_number VARCHAR(20) NOT NULL,
    reservation_id VARCHAR(50) NOT NULL UNIQUE,
    product_type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    loan_number VARCHAR(30),
    status VARCHAR(20) DEFAULT 'PENDING',
    reservation_date DATETIME2 DEFAULT GETDATE(),
    expiry_date DATETIME2 NOT NULL,
    confirmed_date DATETIME2,
    released_date DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_limit_reservations_customer_number ON limit_reservations(customer_number);
CREATE INDEX idx_limit_reservations_reservation_id ON limit_reservations(reservation_id);
CREATE INDEX idx_limit_reservations_status ON limit_reservations(status);
CREATE INDEX idx_limit_reservations_expiry_date ON limit_reservations(expiry_date);

--
-- TABLE: limit_audit
-- Audit trail for limit changes
--
CREATE TABLE limit_audit (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    customer_number VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_limit DECIMAL(19, 2),
    new_limit DECIMAL(19, 2),
    old_used DECIMAL(19, 2),
    new_used DECIMAL(19, 2),
    reference_id VARCHAR(50),
    reference_type VARCHAR(50),
    changed_by VARCHAR(100),
    changed_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_limit_audit_customer_number ON limit_audit(customer_number);
CREATE INDEX idx_limit_audit_changed_at ON limit_audit(changed_at);

--
-- Initial Data
--
INSERT INTO customer_credit_limits (customer_number, total_limit, used_limit, available_limit, risk_score, credit_rating)
SELECT 'CUST-000001', 500000.00, 25000.00, 475000.00, 35, 'AA'
WHERE NOT EXISTS (SELECT 1 FROM customer_credit_limits WHERE customer_number = 'CUST-000001');

INSERT INTO customer_credit_limits (customer_number, total_limit, used_limit, available_limit, risk_score, credit_rating)
SELECT 'CUST-000002', 300000.00, 250000.00, 50000.00, 45, 'A'
WHERE NOT EXISTS (SELECT 1 FROM customer_credit_limits WHERE customer_number = 'CUST-000002');

INSERT INTO customer_credit_limits (customer_number, total_limit, used_limit, available_limit, risk_score, credit_rating)
SELECT 'CUST-000003', 100000.00, 0.00, 100000.00, 25, 'AAA'
WHERE NOT EXISTS (SELECT 1 FROM customer_credit_limits WHERE customer_number = 'CUST-000003');

INSERT INTO product_credit_limits (customer_limit_id, product_type, limit_amount, used_amount, available_amount)
SELECT
    ccl.id, 'PERSONAL',
    CASE WHEN ccl.customer_number = 'CUST-000001' THEN 50000.00
         WHEN ccl.customer_number = 'CUST-000002' THEN 30000.00
         ELSE 20000.00 END,
    0,
    CASE WHEN ccl.customer_number = 'CUST-000001' THEN 50000.00
         WHEN ccl.customer_number = 'CUST-000002' THEN 30000.00
         ELSE 20000.00 END
FROM customer_credit_limits ccl
WHERE ccl.customer_number IN ('CUST-000001', 'CUST-000002', 'CUST-000003');

INSERT INTO product_credit_limits (customer_limit_id, product_type, limit_amount, used_amount, available_amount)
SELECT
    ccl.id, 'MORTGAGE',
    CASE WHEN ccl.customer_number = 'CUST-000001' THEN 450000.00
         WHEN ccl.customer_number = 'CUST-000002' THEN 270000.00
         ELSE 80000.00 END,
    CASE WHEN ccl.customer_number = 'CUST-000001' THEN 0
         WHEN ccl.customer_number = 'CUST-000002' THEN 250000.00
         ELSE 0 END,
    CASE WHEN ccl.customer_number = 'CUST-000001' THEN 450000.00
         WHEN ccl.customer_number = 'CUST-000002' THEN 20000.00
         ELSE 80000.00 END
FROM customer_credit_limits ccl
WHERE ccl.customer_number IN ('CUST-000001', 'CUST-000002', 'CUST-000003');
