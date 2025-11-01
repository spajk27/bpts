package com.bpts.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account information response")
public class AccountResponse {
    
    @Schema(
            description = "Unique account identifier (UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String accountId;
    
    @Schema(
            description = "Account balance",
            example = "1000.00",
            type = "number",
            format = "decimal"
    )
    private BigDecimal balance;
    
    @Schema(
            description = "Account currency code (ISO 4217)",
            example = "USD"
    )
    private String currency;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Account creation timestamp in ISO 8601 format",
            example = "2024-01-15T10:30:00",
            type = "string",
            format = "date-time"
    )
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(
            description = "Last update timestamp in ISO 8601 format",
            example = "2024-01-15T10:30:00",
            type = "string",
            format = "date-time"
    )
    private LocalDateTime updatedAt;
}