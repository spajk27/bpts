package com.bpts.controller;

import com.bpts.model.TransferRequest;
import com.bpts.model.TransferResponse;
import com.bpts.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@DisplayName("TransferController Integration Tests")
class TransferControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransferService transferService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private final String sourceAccountId = UUID.randomUUID().toString();
    private final String destAccountId = UUID.randomUUID().toString();
    
    @Test
    @DisplayName("Should successfully initiate transfer and return 201 CREATED")
    void testInitiateTransferSuccess() throws Exception {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sourceAccountId);
        request.setToAccountId(destAccountId);
        request.setAmount(new BigDecimal("200.00"));
        request.setDescription("Test transfer");
        
        TransferResponse response = TransferResponse.success(
                "transaction-id",
                sourceAccountId,
                destAccountId,
                "200.00",
                "Transfer completed successfully"
        );
        
        when(transferService.transferFunds(any(TransferRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("transaction-id"))
                .andExpect(jsonPath("$.fromAccountId").value(sourceAccountId))
                .andExpect(jsonPath("$.toAccountId").value(destAccountId))
                .andExpect(jsonPath("$.amount").value("200.00"));
    }
    
    @Test
    @DisplayName("Should return 400 BAD REQUEST for invalid transfer request")
    void testInitiateTransferInvalidRequest() throws Exception {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(""); // Invalid: empty account ID
        request.setToAccountId(destAccountId);
        request.setAmount(new BigDecimal("-100.00")); // Invalid: negative amount
        
        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 BAD REQUEST for null request body")
    void testInitiateTransferNullBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 404 NOT FOUND when account does not exist")
    void testInitiateTransferAccountNotFound() throws Exception {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(sourceAccountId);
        request.setToAccountId(destAccountId);
        request.setAmount(new BigDecimal("200.00"));
        
        when(transferService.transferFunds(any(TransferRequest.class)))
                .thenThrow(new com.bpts.exception.AccountNotFoundException("account-id", "Account not found"));
        
        // When & Then
        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }
    
    @Test
    @DisplayName("Should return health check status")
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/transfers/health"))
                .andExpect(status().isOk());
    }
}