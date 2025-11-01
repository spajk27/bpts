package com.bpts.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for creating a new account")
public class CreateAccountRequest {
    
    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    @Schema(
            description = "Initial balance for the account (must be >= 0)",
            example = "1000.00",
            required = false,
            defaultValue = "0.00",
            minimum = "0.00",
            type = "number",
            format = "decimal"
    )
    private BigDecimal initialBalance;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase code (ISO 4217)")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Schema(
            description = "Currency code (ISO 4217 format, 3 uppercase letters)",
            example = "USD",
            required = false,
            defaultValue = "USD",
            pattern = "^[A-Z]{3}$",
            minLength = 3,
            maxLength = 3
    )
    private String currency;
}