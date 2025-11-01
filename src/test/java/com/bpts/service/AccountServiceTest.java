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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    
    @Test
    @DisplayName("Should retrieve all accounts with pagination successfully")
    void testGetAllAccounts() {
        // Given
        Account account1 = Account.builder()
                .accountId("account-1")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();
        
        Account account2 = Account.builder()
                .accountId("account-2")
                .balance(new BigDecimal("2000.00"))
                .currency("USD")
                .build();
        
        List<Account> accounts = Arrays.asList(account1, account2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, 2);
        
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(accountsPage);
        
        // When
        Page<Account> result = accountService.getAllAccounts(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("account-1", result.getContent().get(0).getAccountId());
        assertEquals("account-2", result.getContent().get(1).getAccountId());
        verify(accountRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should return empty page when no accounts exist")
    void testGetAllAccountsEmpty() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);
        
        // When
        Page<Account> result = accountService.getAllAccounts(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        assertTrue(result.getContent().isEmpty());
        verify(accountRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should create account successfully with initial balance and currency")
    void testCreateAccount() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        String currency = "USD";
        
        Account newAccount = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .balance(initialBalance)
                .currency(currency)
                .build();
        
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        
        // When
        Account createdAccount = accountService.createAccount(initialBalance, currency);
        
        // Then
        assertNotNull(createdAccount);
        assertEquals(initialBalance, createdAccount.getBalance());
        assertEquals(currency, createdAccount.getCurrency());
        assertNotNull(createdAccount.getAccountId());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("Should create account with default balance when initialBalance is null")
    void testCreateAccountWithNullBalance() {
        // Given
        String currency = "USD";
        
        Account newAccount = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .build();
        
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        
        // When
        Account createdAccount = accountService.createAccount(null, currency);
        
        // Then
        assertNotNull(createdAccount);
        assertEquals(BigDecimal.ZERO, createdAccount.getBalance());
        assertEquals(currency, createdAccount.getCurrency());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("Should create account with default currency when currency is null")
    void testCreateAccountWithNullCurrency() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        
        Account newAccount = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .balance(initialBalance)
                .currency("USD")
                .build();
        
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        
        // When
        Account createdAccount = accountService.createAccount(initialBalance, null);
        
        // Then
        assertNotNull(createdAccount);
        assertEquals(initialBalance, createdAccount.getBalance());
        assertEquals("USD", createdAccount.getCurrency());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("Should create account with default currency when currency is empty")
    void testCreateAccountWithEmptyCurrency() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        
        Account newAccount = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .balance(initialBalance)
                .currency("USD")
                .build();
        
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);
        
        // When
        Account createdAccount = accountService.createAccount(initialBalance, "");
        
        // Then
        assertNotNull(createdAccount);
        assertEquals(initialBalance, createdAccount.getBalance());
        assertEquals("USD", createdAccount.getCurrency());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when initial balance is negative")
    void testCreateAccountWithNegativeBalance() {
        // Given
        BigDecimal negativeBalance = new BigDecimal("-100.00");
        String currency = "USD";
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(negativeBalance, currency)
        );
        
        assertTrue(exception.getMessage().contains("Initial balance cannot be negative"));
        verify(accountRepository, never()).save(any(Account.class));
    }
}