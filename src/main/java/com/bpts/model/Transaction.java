package com.bpts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_transactions_from_account", columnList = "from_account_id"),
           @Index(name = "idx_transactions_to_account", columnList = "to_account_id"),
           @Index(name = "idx_transactions_date", columnList = "transaction_date"),
           @Index(name = "idx_transactions_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @Column(name = "transaction_id", length = 36, nullable = false)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transactions_from_account"))
    private Account fromAccount;
    
    @Column(name = "from_account_id", insertable = false, updatable = false)
    private String fromAccountId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transactions_to_account"))
    private Account toAccount;
    
    @Column(name = "to_account_id", insertable = false, updatable = false)
    private String toAccountId;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }
    
    public enum TransactionStatus {
        SUCCESS,
        FAILED,
        PENDING
    }
}