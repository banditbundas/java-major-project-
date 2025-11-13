package com.banking.controller;

import com.banking.dto.TransferRequest;
import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.service.AccountService;
import com.banking.service.AuthService;
import com.banking.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService,
                                AccountService accountService,
                                AuthService authService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.authService = authService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferRequest request) {
        try {
            User user = authService.getCurrentUser();
            System.out.println("[TRANSFER] User: " + user.getUsername() + " initiating transfer");
            System.out.println("[TRANSFER] From: " + request.getFromAccountNumber() + " To: " + request.getToAccountNumber());
            System.out.println("[TRANSFER] Amount: " + request.getAmount());
            
            List<com.banking.model.Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            // Verify from account belongs to user
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(request.getFromAccountNumber()));
            
            if (!ownsAccount) {
                System.err.println("[TRANSFER] ERROR: User does not own from account");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }

            Transaction transaction;
            if (request.getIfscCode() != null && !request.getIfscCode().trim().isEmpty()) {
                // External transfer
                System.out.println("[TRANSFER] Processing external transfer with IFSC: " + request.getIfscCode());
                transaction = transactionService.transferToExternalAccount(
                        request.getFromAccountNumber(),
                        request.getToAccountNumber(),
                        request.getIfscCode(),
                        request.getAmount(),
                        request.getDescription()
                );
            } else {
                // Internal transfer
                System.out.println("[TRANSFER] Processing internal transfer");
                transaction = transactionService.transferFunds(
                        request.getFromAccountNumber(),
                        request.getToAccountNumber(),
                        request.getAmount(),
                        request.getDescription()
                );
            }

            System.out.println("[TRANSFER] Success! Transaction ID: " + transaction.getId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.err.println("[TRANSFER] ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountNumber) {
        try {
            User user = authService.getCurrentUser();
            List<com.banking.model.Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }

            List<Transaction> transactions = transactionService.getAccountTransactions(accountNumber);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{accountNumber}/history")
    public ResponseEntity<?> getAccountTransactionsByDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            User user = authService.getCurrentUser();
            List<com.banking.model.Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }

            List<Transaction> transactions = transactionService.getAccountTransactionsByDateRange(
                    accountNumber, startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        try {
            User user = authService.getCurrentUser();
            List<com.banking.model.Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }

            BigDecimal balance = accountService.getAccountBalance(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("accountNumber", accountNumber);
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody Map<String, Object> request) {
        try {
            User user = authService.getCurrentUser();
            String accountNumber = (String) request.get("accountNumber");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = (String) request.get("description");
            
            if (accountNumber == null || amount == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account number and amount are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<com.banking.model.Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Amount must be greater than zero");
                return ResponseEntity.badRequest().body(error);
            }
            
            Transaction transaction = transactionService.deposit(accountNumber, amount, description);
            System.out.println("[DEPOSIT] User: " + user.getUsername() + " deposited " + amount + " to account " + accountNumber);
            
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.err.println("[DEPOSIT] ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

