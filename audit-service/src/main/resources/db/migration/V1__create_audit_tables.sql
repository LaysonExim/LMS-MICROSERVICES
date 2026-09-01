-- Purpose: Audit database schema
-- File: audit-service/src/main/resources/db/migration/V1__create_audit_tables.sql

--
-- TABLE: audit_events
-- Stores all system events for compliance
--
CREATE TABLE audit_events (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    event_id VARCHAR(100) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    event_source VARCHAR(50) NOT NULL,
    event_timestamp DATETIME2 NOT NULL,
    correlation_id VARCHAR(100),

    -- Event data (denormalized for performance)
    loan_number VARCHAR(30),
    customer_number VARCHAR(20),
    user_id VARCHAR(100),
    amount DECIMAL(19, 2),

    -- Additional data (JSON)
    additional_data NVARCHAR(MAX),

    -- Metadata
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Indexes
CREATE INDEX idx_audit_events_loan_number ON audit_events(loan_number);
CREATE INDEX idx_audit_events_customer_number ON audit_events(customer_number);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_event_timestamp ON audit_events(event_timestamp);
CREATE INDEX idx_audit_events_correlation_id ON audit_events(correlation_id);
