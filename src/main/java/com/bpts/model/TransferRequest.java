package com.bpts.model;

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
public class TransferRequest {
    
    @NotBlank(message = "Source account ID is required")
    @Pattern(regexp = "^[a-fA-F0-9\\-]{36}$", message = "Invalid account ID format")
    private String fromAccountId;
    
    @NotBlank(message = "Destination account ID is required")
    @Pattern(regexp = "^[a-fA-F0-9\\-]{36}$", message = "Invalid account ID format")
    private String toAccountId;
    
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    private BigDecimal amount;
    
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