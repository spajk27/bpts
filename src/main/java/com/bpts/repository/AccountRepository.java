package com.bpts.repository;

import com.bpts.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    /**
     * Find account by ID with pessimistic lock for concurrent access control.
     * This ensures thread-safe balance updates during transfers.
     * 
     * @param accountId The account identifier
     * @return Optional Account with lock applied
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountId = :accountId")
    Optional<Account> findByAccountIdWithLock(@Param("accountId") String accountId);
    
    /**
     * Find account by ID without lock (for read-only operations)
     * 
     * @param accountId The account identifier
     * @return Optional Account
     */
    Optional<Account> findByAccountId(String accountId);
    
    /**
     * Check if account exists
     * 
     * @param accountId The account identifier
     * @return true if account exists, false otherwise
     */
    boolean existsByAccountId(String accountId);
}