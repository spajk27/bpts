package com.bpts.service;

import com.bpts.exception.AccountNotFoundException;
import com.bpts.model.Account;
import com.bpts.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private AccountService accountService;
    
    private Account testAccount;
    
    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .accountId("test-account-id")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();
    }
    
    @Test
    @DisplayName("Should retrieve account by ID successfully")
    void testGetAccountById() {
        // Given
        when(accountRepository.findByAccountId("test-account-id"))
                .thenReturn(Optional.of(testAccount));
        
        // When
        Account account = accountService.getAccountById("test-account-id");
        
        // Then
        assertNotNull(account);
        assertEquals("test-account-id", account.getAccountId());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(accountRepository, times(1)).findByAccountId("test-account-id");
    }
    
    @Test
    @DisplayName("Should throw AccountNotFoundException when account does not exist")
    void testGetAccountByIdNotFound() {
        // Given
        when(accountRepository.findByAccountId("non-existent-id"))
                .thenReturn(Optional.empty());
        
        // When & Then
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById("non-existent-id")
        );
        
        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository, times(1)).findByAccountId("non-existent-id");
    }
    
    @Test
    @DisplayName("Should retrieve account with lock successfully")
    void testGetAccountByIdWithLock() {
        // Given
        when(accountRepository.findByAccountIdWithLock("test-account-id"))
                .thenReturn(Optional.of(testAccount));
        
        // When
        Account account = accountService.getAccountByIdWithLock("test-account-id");
        
        // Then
        assertNotNull(account);
        assertEquals("test-account-id", account.getAccountId());
        verify(accountRepository, times(1)).findByAccountIdWithLock("test-account-id");
    }
    
    @Test
    @DisplayName("Should update account balance successfully")
    void testUpdateAccountBalance() {
        // Given
        when(accountRepository.findByAccountIdWithLock("test-account-id"))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        accountService.updateAccountBalance("test-account-id", new BigDecimal("200.00"));
        
        // Then
        assertEquals(new BigDecimal("1200.00"), testAccount.getBalance());
        verify(accountRepository, times(1)).findByAccountIdWithLock("test-account-id");
        verify(accountRepository, times(1)).save(testAccount);
    }
    
    @Test
    @DisplayName("Should throw IllegalStateException when balance would become negative")
    void testUpdateAccountBalanceNegative() {
        // Given
        when(accountRepository.findByAccountIdWithLock("test-account-id"))
                .thenReturn(Optional.of(testAccount));
        
        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> accountService.updateAccountBalance("test-account-id", new BigDecimal("-1500.00"))
        );
        
        assertTrue(exception.getMessage().contains("negative balance"));
        verify(accountRepository, times(1)).findByAccountIdWithLock("test-account-id");
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    @DisplayName("Should check if account exists")
    void testAccountExists() {
        // Given
        when(accountRepository.existsByAccountId("test-account-id")).thenReturn(true);
        when(accountRepository.existsByAccountId("non-existent-id")).thenReturn(false);
        
        // When & Then
        assertTrue(accountService.accountExists("test-account-id"));
        assertFalse(accountService.accountExists("non-existent-id"));
        
        verify(accountRepository, times(2)).existsByAccountId(anyString());
    }
}