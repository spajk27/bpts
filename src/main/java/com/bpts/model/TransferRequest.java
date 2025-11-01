package com.bpts.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for initiating a fund transfer between accounts")
public class TransferRequest {
    
    @NotBlank(message = "Source account ID is required")
    @Pattern(regexp = "^[a-fA-F0-9\\-]{36}$", message = "Invalid account ID format")
    @Schema(
            description = "Source account ID (UUID format)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true,
            pattern = "^[a-fA-F0-9\\-]{36}$"
    )
    private String fromAccountId;
    
    @NotBlank(message = "Destination account ID is required")
    @Pattern(regexp = "^[a-fA-F0-9\\-]{36}$", message = "Invalid account ID format")
    @Schema(
            description = "Destination account ID (UUID format)",
            example = "660e8400-e29b-41d4-a716-446655440001",
            required = true,
            pattern = "^[a-fA-F0-9\\-]{36}$"
    )
    private String toAccountId;
    
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    @Schema(
            description = "Transfer amount (must be greater than 0.01)",
            example = "250.75",
            required = true,
            minimum = "0.01",
            type = "number",
            format = "decimal"
    )
    private BigDecimal amount;
    
    @Schema(
            description = "Optional description for the transfer",
            example = "Payment for invoice #12345",
            required = false
    )
    private String description;
    
    /**
     * Validate that source and destination accounts are different
     */
    public boolean isValidTransfer() {
        return fromAccountId != null && 
               toAccountId != null && 
               !fromAccountId.equals(toAccountId);
    }
}