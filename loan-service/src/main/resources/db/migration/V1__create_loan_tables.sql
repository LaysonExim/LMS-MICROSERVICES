-- Purpose: Initial schema for Loan Service
-- File: loan-service/src/main/resources/db/migration/V1__create_loan_tables.sql

--
-- EXPLANATION: Loan Tables
-- The loan service owns these tables in the loan_db database.
-- No other service can access these tables directly.
--

--
-- TABLE: loan_applications
-- Stores all loan applications
--
CREATE TABLE loan_applications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_number VARCHAR(30) NOT NULL UNIQUE,
    customer_number VARCHAR(20) NOT NULL,
    loan_type VARCHAR(50) NOT NULL,  -- MORTGAGE, PERSONAL, BUSINESS, AUTO, etc.
    loan_purpose VARCHAR(255),
    amount DECIMAL(19, 2) NOT NULL,
    interest_rate DECIMAL(10, 4) NOT NULL,
    term_months INTEGER NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',  -- PENDING, VERIFIED, APPROVED, ACTIVE, CLOSED, REJECTED
    application_date DATETIME2 DEFAULT GETDATE(),
    approved_date DATETIME2,
    disbursement_date DATETIME2,
    closure_date DATETIME2,
    -- Customer information (denormalized for performance)
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    -- Audit fields
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);

-- Indexes
CREATE INDEX idx_loan_applications_customer_number ON loan_applications(customer_number);
CREATE INDEX idx_loan_applications_loan_number ON loan_applications(loan_number);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_application_date ON loan_applications(application_date);
CREATE INDEX idx_loan_applications_loan_type ON loan_applications(loan_type);

--
-- TABLE: loan_schedules
-- Repayment schedule for each loan
--
CREATE TABLE loan_schedules (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    installment_amount DECIMAL(19, 2) NOT NULL,
    principal_amount DECIMAL(19, 2) NOT NULL,
    interest_amount DECIMAL(19, 2) NOT NULL,
    balance_after_installment DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, PAID, OVERDUE, SKIPPED
    paid_date DATE,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_loan_schedule_loan FOREIGN KEY (loan_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_loan_schedules_loan_id ON loan_schedules(loan_id);
CREATE INDEX idx_loan_schedules_due_date ON loan_schedules(due_date);
CREATE INDEX idx_loan_schedules_status ON loan_schedules(status);

--
-- TABLE: loan_repayments
-- Records payments made by customers
--
CREATE TABLE loan_repayments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    repayment_reference VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    principal_amount DECIMAL(19, 2) NOT NULL,
    interest_amount DECIMAL(19, 2) NOT NULL,
    repayment_date DATETIME2 DEFAULT GETDATE(),
    payment_method VARCHAR(30),  -- BANK_TRANSFER, CASH, CHECK, ONLINE
    status VARCHAR(20) DEFAULT 'COMPLETED',  -- PENDING, COMPLETED, FAILED, REVERSED
    notes VARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_loan_repayment_loan FOREIGN KEY (loan_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_loan_repayments_loan_id ON loan_repayments(loan_id);
CREATE INDEX idx_loan_repayments_repayment_reference ON loan_repayments(repayment_reference);
CREATE INDEX idx_loan_repayments_repayment_date ON loan_repayments(repayment_date);

--
-- TABLE: loan_status_history
-- Audit trail for loan status changes
--
CREATE TABLE loan_status_history (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(100),
    changed_at DATETIME2 DEFAULT GETDATE(),
    reason VARCHAR(500),
    CONSTRAINT fk_loan_status_history_loan FOREIGN KEY (loan_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_loan_status_history_loan_id ON loan_status_history(loan_id);
CREATE INDEX idx_loan_status_history_changed_at ON loan_status_history(changed_at);

--
-- TABLE: loan_conditions
-- Stores conditions for loan approval
--
CREATE TABLE loan_conditions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    condition_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, MET, WAIVED, FAILED
    met_date DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_loan_conditions_loan FOREIGN KEY (loan_id) REFERENCES loan_applications(id) ON DELETE CASCADE
);

--
-- Comments for documentation (SQL Server extended properties)
--
EXEC sp_addextendedproperty 'MS_Description', 'Stores all loan applications and their current status', 'SCHEMA', 'dbo', 'TABLE', 'loan_applications';
EXEC sp_addextendedproperty 'MS_Description', 'Business key for the loan (unique across system)', 'SCHEMA', 'dbo', 'TABLE', 'loan_applications', 'COLUMN', 'loan_number';
EXEC sp_addextendedproperty 'MS_Description', 'Reference to the customer (from Customer Service)', 'SCHEMA', 'dbo', 'TABLE', 'loan_applications', 'COLUMN', 'customer_number';
EXEC sp_addextendedproperty 'MS_Description', 'Loan lifecycle status: PENDING, VERIFIED, APPROVED, ACTIVE, CLOSED, REJECTED', 'SCHEMA', 'dbo', 'TABLE', 'loan_applications', 'COLUMN', 'status';
EXEC sp_addextendedproperty 'MS_Description', 'Optimistic locking version field', 'SCHEMA', 'dbo', 'TABLE', 'loan_applications', 'COLUMN', 'version';

EXEC sp_addextendedproperty 'MS_Description', 'Repayment schedule for each loan', 'SCHEMA', 'dbo', 'TABLE', 'loan_schedules';
EXEC sp_addextendedproperty 'MS_Description', 'Records of payments made by customers', 'SCHEMA', 'dbo', 'TABLE', 'loan_repayments';
EXEC sp_addextendedproperty 'MS_Description', 'Audit trail for loan status changes', 'SCHEMA', 'dbo', 'TABLE', 'loan_status_history';
EXEC sp_addextendedproperty 'MS_Description', 'Conditions for loan approval', 'SCHEMA', 'dbo', 'TABLE', 'loan_conditions';

--
-- Initial data (for testing)
--
INSERT INTO loan_applications (loan_number, customer_number, loan_type, loan_purpose, amount, interest_rate, term_months, status, application_date)
VALUES
    ('LN-000001', 'CUST-000001', 'PERSONAL', 'Home renovation', 25000.00, 5.50, 36, 'APPROVED', GETDATE()),
    ('LN-000002', 'CUST-000002', 'MORTGAGE', 'Home purchase', 250000.00, 4.25, 360, 'PENDING', GETDATE());

-- Insert sample schedule for loan LN-000001
DECLARE @loan_id BIGINT = (SELECT id FROM loan_applications WHERE loan_number = 'LN-000001');

IF @loan_id IS NOT NULL
BEGIN
    WITH numbers AS (
        SELECT 1 AS n
        UNION ALL
        SELECT n + 1 FROM numbers WHERE n < 36
    )
    INSERT INTO loan_schedules (loan_id, installment_number, due_date, installment_amount, principal_amount, interest_amount, balance_after_installment, status)
    SELECT
        @loan_id,
        n,
        DATEADD(month, n, CAST(GETDATE() AS DATE)),
        ROUND(25000.00 * (0.055/12.0) / (1.0 - POWER(1.0 + 0.055/12.0, -36)) + 0.01, 2),
        ROUND(25000.00 * (0.055/12.0) / (1.0 - POWER(1.0 + 0.055/12.0, -36)) + 0.01, 2) - ROUND(25000.00 * 0.055/12.0, 2),
        ROUND(25000.00 * 0.055/12.0, 2),
        ROUND(25000.00 - (ROUND(25000.00 * (0.055/12.0) / (1.0 - POWER(1.0 + 0.055/12.0, -36)) + 0.01, 2) - ROUND(25000.00 * 0.055/12.0, 2)), 2),
        'PENDING'
    FROM numbers
    OPTION (MAXRECURSION 36);
END
