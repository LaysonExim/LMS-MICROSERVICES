-- Purpose: Initial schema for Customer Service database (SQL Server)
-- File: customer-service/src/main/resources/db/migration/V1__create_customer_table.sql


---------------------------------------------------------
-- CUSTOMER TABLE
---------------------------------------------------------

IF OBJECT_ID('customers', 'U') IS NULL
    BEGIN

        CREATE TABLE customers (

                                   id BIGINT IDENTITY(1,1) PRIMARY KEY,

                                   customer_number VARCHAR(20) NOT NULL UNIQUE,

                                   first_name VARCHAR(100) NOT NULL,

                                   last_name VARCHAR(100) NOT NULL,

                                   email VARCHAR(255) NOT NULL,

                                   phone_number VARCHAR(20),

                                   date_of_birth DATE,

                                   status VARCHAR(20) DEFAULT 'ACTIVE',

                                   created_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),

                                   updated_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),

                                   created_by VARCHAR(100),

                                   updated_by VARCHAR(100),

                                   version INT DEFAULT 0,


                                   CONSTRAINT uk_customer_email UNIQUE(email),


                                   CONSTRAINT ck_customer_status
                                       CHECK (
                                           status IN
                                           (
                                            'ACTIVE',
                                            'INACTIVE',
                                            'BLOCKED',
                                            'PENDING_VERIFICATION'
                                               )
                                           )

        );

    END;
GO



---------------------------------------------------------
-- CUSTOMER INDEXES
---------------------------------------------------------


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name='idx_customers_customer_number'
)
CREATE INDEX idx_customers_customer_number
    ON customers(customer_number);
GO



IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name='idx_customers_email'
)
CREATE INDEX idx_customers_email
    ON customers(email);
GO



IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name='idx_customers_status'
)
CREATE INDEX idx_customers_status
    ON customers(status);
GO



IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name='idx_customers_created_at'
)
CREATE INDEX idx_customers_created_at
    ON customers(created_at);
GO



IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name='idx_customers_last_name'
)
CREATE INDEX idx_customers_last_name
    ON customers(last_name);
GO




---------------------------------------------------------
-- CUSTOMER AUDIT TABLE
---------------------------------------------------------


IF OBJECT_ID('customer_audit', 'U') IS NULL
    BEGIN


        CREATE TABLE customer_audit (

                                        id BIGINT IDENTITY(1,1) PRIMARY KEY,


                                        customer_id BIGINT NOT NULL,


                                        action VARCHAR(20) NOT NULL,


                                        field_name VARCHAR(100),


                                        old_value NVARCHAR(MAX),


                                        new_value NVARCHAR(MAX),


                                        changed_by VARCHAR(100),


                                        changed_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),



                                        CONSTRAINT fk_customer_audit_customer_id

                                            FOREIGN KEY(customer_id)

                                                REFERENCES customers(id)

                                                ON DELETE CASCADE

        );


    END;
GO



---------------------------------------------------------
-- CUSTOMER AUDIT INDEXES
---------------------------------------------------------


CREATE INDEX idx_customer_audit_customer_id
    ON customer_audit(customer_id);
GO


CREATE INDEX idx_customer_audit_changed_at
    ON customer_audit(changed_at);
GO


CREATE INDEX idx_customer_audit_action
    ON customer_audit(action);
GO




---------------------------------------------------------
-- CUSTOMER UPDATE TRIGGER
---------------------------------------------------------

CREATE OR ALTER TRIGGER trigger_update_customer_audit

    ON customers

    AFTER UPDATE

    AS

BEGIN

    SET NOCOUNT ON;


    UPDATE customers

    SET updated_at = SYSDATETIMEOFFSET()

    FROM customers c

             INNER JOIN inserted i

                        ON c.id = i.id;


END;
GO




---------------------------------------------------------
-- CUSTOMER SEED DATA
---------------------------------------------------------


IF NOT EXISTS
    (
        SELECT 1
        FROM customers
        WHERE customer_number='CUST-000001'
    )

    BEGIN

        INSERT INTO customers
        (
            customer_number,
            first_name,
            last_name,
            email,
            status
        )

        VALUES

            (
                'CUST-000001',
                'John',
                'Doe',
                'john.doe@example.com',
                'ACTIVE'
            );


    END;
GO



IF NOT EXISTS
    (
        SELECT 1
        FROM customers
        WHERE customer_number='CUST-000002'
    )

    BEGIN

        INSERT INTO customers
        (
            customer_number,
            first_name,
            last_name,
            email,
            status
        )

        VALUES

            (
                'CUST-000002',
                'Jane',
                'Smith',
                'jane.smith@example.com',
                'ACTIVE'
            );


    END;
GO





---------------------------------------------------------
-- CUSTOMER ADDRESS TABLE
---------------------------------------------------------


IF OBJECT_ID('customer_addresses','U') IS NULL

    BEGIN


        CREATE TABLE customer_addresses

        (

            id BIGINT IDENTITY(1,1) PRIMARY KEY,


            customer_id BIGINT NOT NULL,


            address_type VARCHAR(20) NOT NULL,


            street VARCHAR(255),


            city VARCHAR(100),


            state VARCHAR(100),


            postal_code VARCHAR(20),


            country VARCHAR(100) DEFAULT 'US',


            is_primary BIT DEFAULT 0,


            created_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),


            updated_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),


            created_by VARCHAR(100),


            updated_by VARCHAR(100),



            CONSTRAINT fk_customer_address_customer_id

                FOREIGN KEY(customer_id)

                    REFERENCES customers(id)

                    ON DELETE CASCADE,



            CONSTRAINT ck_address_type

                CHECK

                    (
                    address_type IN
                    (
                     'HOME',
                     'WORK',
                     'MAILING',
                     'OTHER'
                        )

                    )

        );


    END;
GO




CREATE INDEX idx_customer_addresses_customer_id

    ON customer_addresses(customer_id);
GO



CREATE INDEX idx_customer_addresses_city

    ON customer_addresses(city);
GO






---------------------------------------------------------
-- CUSTOMER VERIFICATION TABLE
---------------------------------------------------------


IF OBJECT_ID('customer_verification','U') IS NULL

    BEGIN


        CREATE TABLE customer_verification

        (

            id BIGINT IDENTITY(1,1) PRIMARY KEY,


            customer_id BIGINT NOT NULL,


            verification_type VARCHAR(50) NOT NULL,


            verification_id VARCHAR(100) NOT NULL,


            verification_status VARCHAR(20) DEFAULT 'PENDING',


            verified_by VARCHAR(100),


            verified_at DATETIMEOFFSET,


            expires_at DATETIMEOFFSET,


            created_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),


            updated_at DATETIMEOFFSET DEFAULT SYSDATETIMEOFFSET(),



            CONSTRAINT fk_customer_verification_customer_id

                FOREIGN KEY(customer_id)

                    REFERENCES customers(id)

                    ON DELETE CASCADE,



            CONSTRAINT uk_verification_unique

                UNIQUE(customer_id, verification_type)


        );


    END;
GO




CREATE INDEX idx_customer_verification_customer_id

    ON customer_verification(customer_id);
GO



CREATE INDEX idx_customer_verification_status

    ON customer_verification(verification_status);
GO