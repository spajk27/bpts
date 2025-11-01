-- Create accounts table
CREATE TABLE accounts (
    account_id VARCHAR(36) PRIMARY KEY COMMENT 'Unique account identifier (UUID)',
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00 COMMENT 'Account balance',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD' COMMENT 'Account currency code (ISO 4217)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation timestamp',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores account information and balances';

-- Create index on created_at for querying recent accounts
CREATE INDEX idx_accounts_created_at ON accounts(created_at);