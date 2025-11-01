package com.bpts.service;

import com.bpts.exception.AccountNotFoundException;
import com.bpts.exception.InsufficientFundsException;
import com.bpts.exception.InvalidTransferException;
import com.bpts.model.Account;
import com.bpts.model.Transaction;
import com.bpts.model.Transaction.TransactionStatus;
import com.bpts.model.TransferRequest;
import com.bpts.model.TransferResponse;
import com.bpts.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    
    /**
     * Transfer funds between accounts
     * 
     * This method ensures atomicity: either both accounts are updated and transaction is recorded,
     * or everything is rolled back on failure.
     * 
     * @param request Transfer request containing source, destination, and amount
     * @return TransferResponse with transaction details
     * @throws AccountNotFoundException if source or destination account does not exist
     * @throws InsufficientFundsException if source account has insufficient funds
     * @throws InvalidTransferException if transfer violates business rules
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse transferFunds(TransferRequest request) {
        log.info("Initiating transfer: {} -> {} amount: {}", 
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());
        
        // Validate transfer request
        validateTransferRequest(request);
        
        // Get accounts with pessimistic lock to prevent concurrent modifications
        Account fromAccount = accountService.getAccountByIdWithLock(request.getFromAccountId());
        Account toAccount = accountService.getAccountByIdWithLock(request.getToAccountId());
        
        // Validate sufficient funds
        validateSufficientFunds(fromAccount, request.getAmount());
        
        // Validate accounts are different
        if (fromAccount.getAccountId().equals(toAccount.getAccountId())) {
            throw new InvalidTransferException("Source and destination accounts cannot be the same");
        }
        
        // Validate currencies match (if different currencies, exchange rate logic would be needed)
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new InvalidTransferException(
                    String.format("Currency mismatch: %s vs %s. Cross-currency transfers not supported.",
                            fromAccount.getCurrency(), toAccount.getCurrency()));
        }
        
        // Create transaction record with PENDING status
        Transaction transaction = createTransactionRecord(fromAccount, toAccount, request);
        
        try {
            // Execute transfer: deduct from source, add to destination
            BigDecimal transferAmount = request.getAmount();
            
            accountService.updateAccountBalance(fromAccount.getAccountId(), 
                    transferAmount.negate()); // Deduct from source
            accountService.updateAccountBalance(toAccount.getAccountId(), 
                    transferAmount); // Add to destination
            
            // Update transaction status to SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
            
            log.info("Transfer completed successfully. Transaction ID: {}", transaction.getTransactionId());
            
            return TransferResponse.success(
                    transaction.getTransactionId(),
                    fromAccount.getAccountId(),
                    toAccount.getAccountId(),
                    transferAmount.toString(),
                    "Transfer completed successfully"
            );
            
        } catch (Exception e) {
            // Mark transaction as FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            
            log.error("Transfer failed for transaction {}: {}", transaction.getTransactionId(), e.getMessage(), e);
            
            // Re-throw exception to trigger transaction rollback
            throw new InvalidTransferException("Transfer failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate transfer request
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new InvalidTransferException("Transfer request cannot be null");
        }
        
        if (!request.isValidTransfer()) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }
        
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero");
        }
        
        // Validate account IDs format (basic UUID validation)
        if (request.getFromAccountId() == null || request.getFromAccountId().trim().isEmpty()) {
            throw new InvalidTransferException("Source account ID is required");
        }
        
        if (request.getToAccountId() == null || request.getToAccountId().trim().isEmpty()) {
            throw new InvalidTransferException("Destination account ID is required");
        }
    }
    
    /**
     * Validate that source account has sufficient funds
     */
    private void validateSufficientFunds(Account account, BigDecimal amount) {
        if (!account.hasSufficientBalance(amount)) {
            throw new InsufficientFundsException(
                    account.getAccountId(),
                    account.getBalance(),
                    amount
            );
        }
    }
    
    /**
     * Create transaction record with PENDING status
     */
    private Transaction createTransactionRecord(Account fromAccount, Account toAccount, TransferRequest request) {
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .fromAccountId(fromAccount.getAccountId())
                .toAccount(toAccount)
                .toAccountId(toAccount.getAccountId())
                .amount(request.getAmount())
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.debug("Created transaction record: {}", savedTransaction.getTransactionId());
        return savedTransaction;
    }
}