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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {
    
    @Mock
    private AccountService accountService;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private TransferService transferService;
    
    private Account sourceAccount;
    private Account destinationAccount;
    private TransferRequest validTransferRequest;
    private String sourceAccountId;
    private String destAccountId;
    
    @BeforeEach
    void setUp() {
        sourceAccountId = UUID.randomUUID().toString();
        destAccountId = UUID.randomUUID().toString();

        sourceAccount = Account.builder()
                .accountId(sourceAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();
        
        destinationAccount = Account.builder()
                .accountId(destAccountId)
                .balance(new BigDecimal("500.00"))
                .currency("USD")
                .build();
        
        validTransferRequest = new TransferRequest();
        validTransferRequest.setFromAccountId(sourceAccountId);
        validTransferRequest.setToAccountId(destAccountId);
        validTransferRequest.setAmount(new BigDecimal("200.00"));
        validTransferRequest.setDescription("Test transfer");
    }
    
    @Test
    @DisplayName("Should successfully transfer funds between accounts")
    void testSuccessfulTransfer() {
        // Given
        when(accountService.getAccountByIdWithLock(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountByIdWithLock(destAccountId)).thenReturn(destinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setTransactionId("transaction-id");
            return transaction;
        });
        doNothing().when(accountService).updateAccountBalance(anyString(), any(BigDecimal.class));
        
        // When
        TransferResponse response = transferService.transferFunds(validTransferRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getTransactionId());
        assertEquals(sourceAccountId, response.getFromAccountId());
        assertEquals(destAccountId, response.getToAccountId());
        
        verify(accountService, times(1)).getAccountByIdWithLock(sourceAccountId);
        verify(accountService, times(1)).getAccountByIdWithLock(destAccountId);
        verify(accountService, times(1)).updateAccountBalance(sourceAccountId, new BigDecimal("-200.00"));
        verify(accountService, times(1)).updateAccountBalance(destAccountId, new BigDecimal("200.00"));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should throw InsufficientFundsException when source account has insufficient funds")
    void testInsufficientFunds() {
        // Given
        sourceAccount.setBalance(new BigDecimal("100.00"));
        when(accountService.getAccountByIdWithLock(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountByIdWithLock(destAccountId)).thenReturn(destinationAccount);
        
        // When & Then
        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("Insufficient funds"));
        assertEquals(sourceAccountId, exception.getAccountId());
        
        verify(accountService, times(1)).getAccountByIdWithLock(sourceAccountId);
        verify(accountService, times(1)).getAccountByIdWithLock(destAccountId);
        verify(accountService, never()).updateAccountBalance(anyString(), any(BigDecimal.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should throw AccountNotFoundException when source account does not exist")
    void testSourceAccountNotFound() {
        // Given
        when(accountService.getAccountByIdWithLock(sourceAccountId))
                .thenThrow(new AccountNotFoundException(sourceAccountId, "Account not found"));
        
        // When & Then
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountService, times(1)).getAccountByIdWithLock(sourceAccountId);
        verify(accountService, never()).updateAccountBalance(anyString(), any(BigDecimal.class));
    }
    
    @Test
    @DisplayName("Should throw AccountNotFoundException when destination account does not exist")
    void testDestinationAccountNotFound() {
        // Given
        when(accountService.getAccountByIdWithLock(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountByIdWithLock(destAccountId))
                .thenThrow(new AccountNotFoundException(destAccountId, "Account not found"));
        
        // When & Then
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountService, times(1)).getAccountByIdWithLock(sourceAccountId);
        verify(accountService, times(1)).getAccountByIdWithLock(destAccountId);
        verify(accountService, never()).updateAccountBalance(anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Should throw InvalidTransferException when source and destination accounts are the same")
    void testSameAccountTransfer() {
        // Given
        String sameAccountId = UUID.randomUUID().toString();
        validTransferRequest.setFromAccountId(sameAccountId);
        validTransferRequest.setToAccountId(sameAccountId);

        // When & Then
        InvalidTransferException exception = assertThrows(
                InvalidTransferException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );

        assertTrue(exception.getMessage().contains("cannot be the same") ||
                exception.getMessage().contains("must be different"));
        // Accounts are never retrieved because validation fails early in validateTransferRequest()
        verify(accountService, never()).getAccountByIdWithLock(anyString());
        verify(accountService, never()).updateAccountBalance(anyString(), any(BigDecimal.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should throw InvalidTransferException when transfer amount is zero")
    void testZeroAmountTransfer() {
        // Given
        validTransferRequest.setAmount(BigDecimal.ZERO);
        
        // When & Then
        InvalidTransferException exception = assertThrows(
                InvalidTransferException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("greater than zero"));
        verify(accountService, never()).getAccountByIdWithLock(anyString());
    }
    
    @Test
    @DisplayName("Should throw InvalidTransferException when transfer amount is negative")
    void testNegativeAmountTransfer() {
        // Given
        validTransferRequest.setAmount(new BigDecimal("-100.00"));
        
        // When & Then
        InvalidTransferException exception = assertThrows(
                InvalidTransferException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("greater than zero"));
        verify(accountService, never()).getAccountByIdWithLock(anyString());
    }

    @Test
    @DisplayName("Should throw InvalidTransferException when currencies do not match")
    void testCurrencyMismatch() {
        // Given
        sourceAccount.setCurrency("USD");
        destinationAccount.setCurrency("EUR");

        when(accountService.getAccountByIdWithLock(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountByIdWithLock(destAccountId)).thenReturn(destinationAccount);
        // Transaction is never created because exception is thrown during currency validation

        // When & Then
        InvalidTransferException exception = assertThrows(
                InvalidTransferException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );

        assertTrue(exception.getMessage().contains("Currency mismatch"));
        verify(accountService, times(1)).getAccountByIdWithLock(sourceAccountId);
        verify(accountService, times(1)).getAccountByIdWithLock(destAccountId);
        verify(accountService, never()).updateAccountBalance(anyString(), any(BigDecimal.class));
        // Transaction is never created because exception is thrown before transaction creation
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should mark transaction as FAILED when balance update fails")
    void testFailedTransactionMarking() {
        // Given
        when(accountService.getAccountByIdWithLock(sourceAccountId)).thenReturn(sourceAccount);
        when(accountService.getAccountByIdWithLock(destAccountId)).thenReturn(destinationAccount);

        // Use a list to capture transaction statuses at the time of save
        final java.util.List<TransactionStatus> savedStatuses = new java.util.ArrayList<>();

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(transactionRepository.save(transactionCaptor.capture())).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            // Capture the status at the time of save
            savedStatuses.add(transaction.getStatus());
            if (transaction.getTransactionId() == null) {
                transaction.setTransactionId("transaction-id");
            }
            return transaction;
        });

        doThrow(new IllegalStateException("Balance update failed"))
                .when(accountService).updateAccountBalance(sourceAccountId, new BigDecimal("-200.00"));

        // When & Then
        InvalidTransferException exception = assertThrows(
                InvalidTransferException.class,
                () -> transferService.transferFunds(validTransferRequest)
        );

        // Verify transaction is saved twice: once for creation (PENDING), once for failure (FAILED)
        verify(transactionRepository, times(2)).save(any(Transaction.class));

        // Verify the statuses captured at save time
        assertEquals(2, savedStatuses.size());
        assertEquals(TransactionStatus.PENDING, savedStatuses.get(0));
        assertEquals(TransactionStatus.FAILED, savedStatuses.get(1));

        // Verify both transactions were saved (even though they're the same object reference)
        assertEquals(2, transactionCaptor.getAllValues().size());
        assertNotNull(transactionCaptor.getAllValues().get(1).getTransactionId());
    }
}