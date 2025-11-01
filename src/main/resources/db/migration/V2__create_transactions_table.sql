-- Create transactions table
CREATE TABLE transactions (
    transaction_id VARCHAR(36) PRIMARY KEY COMMENT 'Unique transaction identifier (UUID)',
    from_account_id VARCHAR(36) NOT NULL COMMENT 'Source account ID',
    to_account_id VARCHAR(36) NOT NULL COMMENT 'Destination account ID',
    amount DECIMAL(19, 2) NOT NULL COMMENT 'Transfer amount',
    status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Transaction status',
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Transaction timestamp',
    description VARCHAR(500) DEFAULT NULL COMMENT 'Optional transaction description',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    CONSTRAINT chk_amount_positive CHECK (amount > 0) COMMENT 'Ensure transfer amount is positive',
    CONSTRAINT chk_different_accounts CHECK (from_account_id != to_account_id) COMMENT 'Prevent transfers to same account',
    CONSTRAINT fk_transactions_from_account FOREIGN KEY (from_account_id) 
        REFERENCES accounts(account_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_transactions_to_account FOREIGN KEY (to_account_id) 
        REFERENCES accounts(account_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_transactions_from_account (from_account_id) COMMENT 'Index for querying by source account',
    INDEX idx_transactions_to_account (to_account_id) COMMENT 'Index for querying by destination account',
    INDEX idx_transactions_date (transaction_date) COMMENT 'Index for querying by transaction date',
    INDEX idx_transactions_status (status) COMMENT 'Index for querying by transaction status'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores all transfer transaction records';