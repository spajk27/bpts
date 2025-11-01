package com.bpts.repository;

import com.bpts.model.Transaction;
import com.bpts.model.Transaction.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    /**
     * Find all transactions for a specific account (both incoming and outgoing)
     * 
     * @param accountId The account identifier
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllByAccountId(@Param("accountId") String accountId, Pageable pageable);
    
    /**
     * Find all outgoing transactions for an account
     * 
     * @param accountId The source account identifier
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByFromAccountIdOrderByTransactionDateDesc(String accountId, Pageable pageable);
    
    /**
     * Find all incoming transactions for an account
     * 
     * @param accountId The destination account identifier
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByToAccountIdOrderByTransactionDateDesc(String accountId, Pageable pageable);
    
    /**
     * Find transactions by status
     * 
     * @param status The transaction status
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    Page<Transaction> findByStatusOrderByTransactionDateDesc(TransactionStatus status, Pageable pageable);
    
    /**
     * Find transactions within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    /**
     * Find transactions for an account within a date range
     * 
     * @param accountId The account identifier
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findAccountTransactionsByDateRange(
            @Param("accountId") String accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    /**
     * Count transactions by status
     * 
     * @param status The transaction status
     * @return Count of transactions with the given status
     */
    long countByStatus(TransactionStatus status);
    
    /**
     * Find all successful transactions for an account
     * 
     * @param accountId The account identifier
     * @return List of successful transactions
     */
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.status = 'SUCCESS' ORDER BY t.transactionDate DESC")
    List<Transaction> findSuccessfulTransactionsByAccountId(@Param("accountId") String accountId);
}