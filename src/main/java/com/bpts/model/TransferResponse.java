package com.bpts.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response model for fund transfer operation")
public class TransferResponse {
    
    @Schema(
            description = "Unique transaction identifier (UUID)",
            example = "770e8400-e29b-41d4-a716-446655440002"
    )
    private String transactionId;
    
    @Schema(
            description = "Transaction status",
            example = "SUCCESS",
            allowableValues = {"SUCCESS", "FAILED", "PENDING"}
    )
    private String status;
    
    @Schema(
            description = "Human-readable message describing the transaction result",
            example = "Transfer completed successfully"
    )
    private String message;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Transaction timestamp in ISO 8601 format",
            example = "2024-01-15T10:30:00",
            type = "string",
            format = "date-time"
    )
    private LocalDateTime timestamp;
    
    @Schema(
            description = "Source account ID",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String fromAccountId;
    
    @Schema(
            description = "Destination account ID",
            example = "660e8400-e29b-41d4-a716-446655440001"
    )
    private String toAccountId;
    
    @Schema(
            description = "Transfer amount",
            example = "250.75",
            type = "string"
    )
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