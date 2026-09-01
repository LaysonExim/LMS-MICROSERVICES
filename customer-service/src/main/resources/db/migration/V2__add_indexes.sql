-- Purpose: Additional indexes for query performance
-- File: customer-service/src/main/resources/db/migration/V2__add_indexes.sql
-- Database: Microsoft SQL Server


---------------------------------------------------------
-- Index for searching customers by full name
---------------------------------------------------------

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customers_first_name_last_name'
)
BEGIN

CREATE INDEX idx_customers_first_name_last_name

    ON customers
        (
         first_name,
         last_name
            );

END;
GO



---------------------------------------------------------
-- Index for sorting customers by creation date
---------------------------------------------------------

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customers_created_at_desc'
)
BEGIN

CREATE INDEX idx_customers_created_at_desc

    ON customers
        (
         created_at DESC
            );

END;
GO




---------------------------------------------------------
-- Filtered Index for ACTIVE customers only
--
-- SQL Server equivalent of PostgreSQL partial index
---------------------------------------------------------

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customers_active'
)
BEGIN

CREATE INDEX idx_customers_active

    ON customers
        (
         customer_number
            )

    WHERE status = 'ACTIVE';

END;
GO




---------------------------------------------------------
-- Composite index for status and creation date queries
---------------------------------------------------------

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customers_status_created'
)
BEGIN

CREATE INDEX idx_customers_status_created

    ON customers
        (
         status,
         created_at
            );

END;
GO




---------------------------------------------------------
-- Case insensitive email search index
--
-- SQL Server normally handles this using collation.
-- Most SQL Server installations use CI collation:
-- SQL_Latin1_General_CP1_CI_AS
--
-- We create a computed column for better control.
---------------------------------------------------------


IF COL_LENGTH('customers','email_lower') IS NULL

BEGIN

ALTER TABLE customers

    ADD email_lower AS LOWER(email);

END;
GO



IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customers_email_lower'
)

BEGIN

CREATE INDEX idx_customers_email_lower

    ON customers(email_lower);

END;
GO





---------------------------------------------------------
-- Audit performance index
--
-- Used for regulatory audit searches
---------------------------------------------------------

IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customer_audit_customer_id_changed_at'
)

BEGIN

CREATE INDEX idx_customer_audit_customer_id_changed_at

    ON customer_audit
        (
         customer_id,
         changed_at DESC
            );

END;
GO





---------------------------------------------------------
-- Address query performance index
--
-- SQL Server filtered index equivalent
-- PostgreSQL:
-- WHERE is_primary = true
--
-- SQL Server:
-- WHERE is_primary = 1
---------------------------------------------------------


IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = 'idx_customer_addresses_city_country'
)

BEGIN

CREATE INDEX idx_customer_addresses_city_country

    ON customer_addresses
        (
         city,
         country
            )

    WHERE is_primary = 1;

END;
GO