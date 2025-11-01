package com.bpts.controller;

import com.bpts.exception.AccountNotFoundException;
import com.bpts.model.Account;
import com.bpts.model.CreateAccountRequest;
import com.bpts.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("AccountController Integration Tests")
class AccountControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AccountService accountService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final String testAccountId = UUID.randomUUID().toString();
    private final String testAccountId2 = UUID.randomUUID().toString();
    
    @Test
    @DisplayName("Should successfully get all accounts with pagination and return 200 OK")
    void testGetAllAccountsSuccess() throws Exception {
        // Given
        Account account1 = Account.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Account account2 = Account.builder()
                .accountId(testAccountId2)
                .balance(new BigDecimal("2500.50"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<Account> accounts = Arrays.asList(account1, account2);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, 2);
        
        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(accountsPage);
        
        // When & Then
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountId").value(testAccountId))
                .andExpect(jsonPath("$.content[0].balance").value(1000.00))
                .andExpect(jsonPath("$.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.content[1].accountId").value(testAccountId2))
                .andExpect(jsonPath("$.content[1].balance").value(2500.50))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }
    
    @Test
    @DisplayName("Should successfully get all accounts with pagination parameters")
    void testGetAllAccountsWithPagination() throws Exception {
        // Given
        Account account = Account.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<Account> accounts = Arrays.asList(account);
        Pageable pageable = PageRequest.of(1, 10);
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, 25);
        
        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(accountsPage);
        
        // When & Then
        mockMvc.perform(get("/api/accounts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(1));
    }
    
    @Test
    @DisplayName("Should successfully get account by ID and return 200 OK")
    void testGetAccountByIdSuccess() throws Exception {
        // Given
        Account account = Account.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(accountService.getAccountById(testAccountId)).thenReturn(account);
        
        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
    
    @Test
    @DisplayName("Should return 404 NOT FOUND when account does not exist")
    void testGetAccountByIdNotFound() throws Exception {
        // Given
        when(accountService.getAccountById(testAccountId))
                .thenThrow(new AccountNotFoundException(testAccountId, "Account not found"));
        
        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}", testAccountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }
    
    @Test
    @DisplayName("Should successfully create account and return 201 CREATED")
    void testCreateAccountSuccess() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest();
        request.setInitialBalance(new BigDecimal("1000.00"));
        request.setCurrency("USD");
        
        Account createdAccount = Account.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(accountService.createAccount(eq(new BigDecimal("1000.00")), eq("USD")))
                .thenReturn(createdAccount);
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
    
    @Test
    @DisplayName("Should successfully create account with defaults and return 201 CREATED")
    void testCreateAccountWithDefaults() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest();
        // No initialBalance or currency set - should use defaults
        
        Account createdAccount = Account.builder()
                .accountId(testAccountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(accountService.createAccount(eq(BigDecimal.ZERO), eq("USD")))
                .thenReturn(createdAccount);
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
    
    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid create account request")
    void testCreateAccountInvalidRequest() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest();
        request.setInitialBalance(new BigDecimal("-100.00")); // Invalid: negative balance
        request.setCurrency("INVALID"); // Invalid: not 3 uppercase letters
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should successfully create account with empty request body using defaults")
    void testCreateAccountNullBody() throws Exception {
        // Given
        Account createdAccount = Account.builder()
                .accountId(testAccountId)
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(accountService.createAccount(eq(BigDecimal.ZERO), eq("USD")))
                .thenReturn(createdAccount);
        
        // When & Then
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated()) // Empty body is valid (uses defaults)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value(testAccountId))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
    
    @Test
    @DisplayName("Should successfully check account exists and return 200 OK with true")
    void testCheckAccountExistsTrue() throws Exception {
        // Given
        when(accountService.accountExists(testAccountId)).thenReturn(true);
        
        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}/exists", testAccountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(true));
    }
    
    @Test
    @DisplayName("Should successfully check account exists and return 200 OK with false")
    void testCheckAccountExistsFalse() throws Exception {
        // Given
        when(accountService.accountExists(testAccountId)).thenReturn(false);
        
        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}/exists", testAccountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exists").value(false));
    }
    
    @Test
    @DisplayName("Should return empty page when no accounts exist")
    void testGetAllAccountsEmpty() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(emptyPage);
        
        // When & Then
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }
    
    @Test
    @DisplayName("Should handle sorting parameter correctly")
    void testGetAllAccountsWithSorting() throws Exception {
        // Given
        Account account1 = Account.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        List<Account> accounts = Arrays.asList(account1);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, 1);
        
        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(accountsPage);
        
        // When & Then
        mockMvc.perform(get("/api/accounts")
                        .param("sort", "balance,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}

