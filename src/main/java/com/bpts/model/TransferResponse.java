package com.bpts.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {
    
    private String transactionId;
    private String status;
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String fromAccountId;
    private String toAccountId;
    private String amount;
    
    public static TransferResponse success(String transactionId, String fromAccountId, 
                                          String toAccountId, String amount, String message) {
        return TransferResponse.builder()
                .transactionId(transactionId)
                .status("SUCCESS")
                .message(message != null ? message : "Transfer completed successfully")
                .timestamp(LocalDateTime.now())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();
    }
    
    public static TransferResponse failure(String message) {
        return TransferResponse.builder()
                .status("FAILED")
                .message(message != null ? message : "Transfer failed")
                .timestamp(LocalDateTime.now())
                .build();
    }
}