package com.bpts.controller;

import com.bpts.exception.GlobalExceptionHandler;
import com.bpts.model.Account;
import com.bpts.model.AccountResponse;
import com.bpts.model.CreateAccountRequest;
import com.bpts.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Account",
        description = "Account management API endpoints for account operations"
)
public class AccountController {
    
    private final AccountService accountService;
    
    /**
     * Get all accounts with pagination support
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of AccountResponse
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get all accounts",
            description = "Retrieves a paginated list of all accounts. " +
                         "Supports pagination (page, size) and sorting. " +
                         "Default page size is 20 accounts per page, sorted by creation date descending."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Paginated Accounts",
                                    value = "{\n" +
                                            "  \"content\": [\n" +
                                            "    {\n" +
                                            "      \"accountId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                                            "      \"balance\": 1000.00,\n" +
                                            "      \"currency\": \"USD\",\n" +
                                            "      \"createdAt\": \"2024-01-15T10:30:00\",\n" +
                                            "      \"updatedAt\": \"2024-01-15T10:30:00\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"accountId\": \"660e8400-e29b-41d4-a716-446655440001\",\n" +
                                            "      \"balance\": 2500.50,\n" +
                                            "      \"currency\": \"USD\",\n" +
                                            "      \"createdAt\": \"2024-01-15T11:00:00\",\n" +
                                            "      \"updatedAt\": \"2024-01-15T11:00:00\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"totalElements\": 2,\n" +
                                            "  \"totalPages\": 1,\n" +
                                            "  \"size\": 20,\n" +
                                            "  \"number\": 0,\n" +
                                            "  \"first\": true,\n" +
                                            "  \"last\": true,\n" +
                                            "  \"numberOfElements\": 2\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid pagination parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    @Parameter(
            name = "page",
            description = "Page number (zero-based, default: 0)",
            example = "0",
            schema = @Schema(type = "integer", defaultValue = "0")
    )
    @Parameter(
            name = "size",
            description = "Page size (default: 20, max: 100)",
            example = "20",
            schema = @Schema(type = "integer", defaultValue = "20", maximum = "100")
    )
    @Parameter(
            name = "sort",
            description = "Sorting criteria (format: property,direction). Example: createdAt,desc or balance,asc",
            example = "createdAt,desc",
            schema = @Schema(type = "string")
    )
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        log.info("Retrieving all accounts with pagination: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        
        Page<Account> accountsPage = accountService.getAllAccounts(pageable);
        Page<AccountResponse> responsePage = accountsPage.map(this::mapToAccountResponse);
        
        log.info("Retrieved {} accounts (page {} of {})", 
                accountsPage.getNumberOfElements(), 
                accountsPage.getNumber() + 1, 
                accountsPage.getTotalPages());
        
        return ResponseEntity.ok(responsePage);
    }
    
    /**
     * Get account by ID
     * 
     * @param accountId The account identifier
     * @return AccountResponse with account details
     */
    @GetMapping(
            value = "/{accountId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Get account by ID",
            description = "Retrieves account information by account ID. " +
                         "Returns account details including balance, currency, and timestamps."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(
                                    name = "Account Response",
                                    value = "{\n" +
                                            "  \"accountId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                                            "  \"balance\": 1000.00,\n" +
                                            "  \"currency\": \"USD\",\n" +
                                            "  \"createdAt\": \"2024-01-15T10:30:00\",\n" +
                                            "  \"updatedAt\": \"2024-01-15T10:30:00\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Account Not Found",
                                    value = "{\n" +
                                            "  \"error\": \"Account Not Found\",\n" +
                                            "  \"message\": \"Account 550e8400-e29b-41d4-a716-446655440000: Account not found\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00\",\n" +
                                            "  \"status\": 404\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid account ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(
                    description = "Account identifier (UUID)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String accountId
    ) {
        log.info("Retrieving account: {}", accountId);
        
        Account account = accountService.getAccountById(accountId);
        AccountResponse response = mapToAccountResponse(account);
        
        log.info("Account retrieved successfully: {}", accountId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new account
     * 
     * @param request Create account request with initial balance and currency
     * @return AccountResponse with created account details
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Create a new account",
            description = "Creates a new account with an optional initial balance and currency. " +
                         "If no initial balance is provided, defaults to 0.00. " +
                         "If no currency is provided, defaults to USD."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(
                                    name = "Created Account",
                                    value = "{\n" +
                                            "  \"accountId\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                                            "  \"balance\": 1000.00,\n" +
                                            "  \"currency\": \"USD\",\n" +
                                            "  \"createdAt\": \"2024-01-15T10:30:00\",\n" +
                                            "  \"updatedAt\": \"2024-01-15T10:30:00\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid input (e.g., negative balance, invalid currency format)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = "{\n" +
                                            "  \"error\": \"Validation Failed\",\n" +
                                            "  \"message\": \"Invalid input parameters\",\n" +
                                            "  \"timestamp\": \"2024-01-15T10:30:00\",\n" +
                                            "  \"status\": 400,\n" +
                                            "  \"details\": {\n" +
                                            "    \"initialBalance\": \"Initial balance cannot be negative\",\n" +
                                            "    \"currency\": \"Currency must be a 3-letter uppercase code (ISO 4217)\"\n" +
                                            "  }\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody(
                    description = "Create account request with optional initial balance and currency",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateAccountRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Account with Initial Balance",
                                            summary = "Create account with initial balance",
                                            value = "{\n" +
                                                    "  \"initialBalance\": 1000.00,\n" +
                                                    "  \"currency\": \"USD\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Empty Account",
                                            summary = "Create account with zero balance",
                                            value = "{\n" +
                                                    "  \"initialBalance\": 0.00,\n" +
                                                    "  \"currency\": \"USD\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "Minimal Request",
                                            summary = "Create account with defaults",
                                            value = "{}"
                                    )
                            }
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateAccountRequest request
    ) {
        log.info("Creating new account with initial balance: {}, currency: {}", 
                request.getInitialBalance(), request.getCurrency());
        
        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance() 
                : java.math.BigDecimal.ZERO;
        String currency = request.getCurrency() != null && !request.getCurrency().isEmpty()
                ? request.getCurrency()
                : "USD";
        
        Account account = accountService.createAccount(initialBalance, currency);
        AccountResponse response = mapToAccountResponse(account);
        
        log.info("Account created successfully: {}", account.getAccountId());
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    /**
     * Check if account exists
     * 
     * @param accountId The account identifier
     * @return ResponseEntity with exists status
     */
    @GetMapping(
            value = "/{accountId}/exists",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Check if account exists",
            description = "Checks if an account with the given ID exists. " +
                         "Returns a boolean indicating existence status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Existence check completed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Account Exists",
                                            value = "{\"exists\": true}"
                                    ),
                                    @ExampleObject(
                                            name = "Account Does Not Exist",
                                            value = "{\"exists\": false}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Invalid account ID format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<java.util.Map<String, Boolean>> checkAccountExists(
            @Parameter(
                    description = "Account identifier (UUID)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable String accountId
    ) {
        log.debug("Checking if account exists: {}", accountId);
        
        boolean exists = accountService.accountExists(accountId);
        
        return ResponseEntity.ok(java.util.Map.of("exists", exists));
    }
    
    /**
     * Map Account entity to AccountResponse DTO
     */
    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}