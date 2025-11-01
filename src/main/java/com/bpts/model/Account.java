package com.bpts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", 
       indexes = @Index(name = "idx_accounts_created_at", columnList = "created_at"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    
    @Id
    @Column(name = "account_id", length = 36, nullable = false)
    private String accountId;
    
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if account has sufficient balance for transfer
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}