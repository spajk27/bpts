package com.bpts.service;

import com.bpts.exception.AccountNotFoundException;
import com.bpts.model.Account;
import com.bpts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository accountRepository;
    
    /**
     * Get account by ID
     * 
     * @param accountId The account identifier
     * @return Account entity
     * @throws AccountNotFoundException if account does not exist
     */
    @Transactional(readOnly = true)
    public Account getAccountById(String accountId) {
        log.debug("Retrieving account: {}", accountId);
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId, "Account not found"));
    }
    
    /**
     * Get account by ID with pessimistic lock for concurrent access control
     * 
     * @param accountId The account identifier
     * @return Account entity with lock applied
     * @throws AccountNotFoundException if account does not exist
     */
    @Transactional
    public Account getAccountByIdWithLock(String accountId) {
        log.debug("Retrieving account with lock: {}", accountId);
        return accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId, "Account not found"));
    }
    
    /**
     * Check if account exists
     * 
     * @param accountId The account identifier
     * @return true if account exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean accountExists(String accountId) {
        return accountRepository.existsByAccountId(accountId);
    }
    
    /**
     * Update account balance atomically
     * 
     * @param accountId The account identifier
     * @param amount The amount to add (positive) or subtract (negative)
     * @throws AccountNotFoundException if account does not exist
     * @throws IllegalStateException if balance would become negative
     */
    @Transactional
    public void updateAccountBalance(String accountId, BigDecimal amount) {
        log.debug("Updating balance for account {} by amount: {}", accountId, amount);
        Account account = getAccountByIdWithLock(accountId);
        
        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    String.format("Balance update would result in negative balance for account %s", accountId));
        }
        
        account.setBalance(newBalance);
        accountRepository.save(account);
        log.info("Balance updated for account {}: new balance = {}", accountId, newBalance);
    }
    
    /**
     * Create a new account with initial balance
     * 
     * @param initialBalance The initial balance
     * @param currency The currency code
     * @return Created Account entity
     */
    @Transactional
    public Account createAccount(BigDecimal initialBalance, String currency) {
        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        if (currency == null || currency.isEmpty()) {
            currency = "USD";
        }
        
        Account account = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .balance(initialBalance)
                .currency(currency)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        log.info("Created new account: {} with initial balance: {}", savedAccount.getAccountId(), initialBalance);
        return savedAccount;
    }
}