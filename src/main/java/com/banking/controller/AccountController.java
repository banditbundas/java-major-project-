package com.banking.controller;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.service.AccountService;
import com.banking.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    public AccountController(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts() {
        try {
            User user = authService.getCurrentUser();
            System.out.println("[ACCOUNTS] Fetching accounts for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            List<Account> accounts = accountService.getUserAccounts(user.getId());
            
            System.out.println("[ACCOUNTS] Found " + accounts.size() + " accounts");
            accounts.forEach(acc -> {
                System.out.println("[ACCOUNTS]   - " + acc.getAccountNumber() + " (" + acc.getAccountType() + ") Balance: " + acc.getBalance());
            });
            
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            System.err.println("[ACCOUNTS] ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestParam String accountType,
                                          @RequestParam(required = false) String accountName) {
        try {
            User user = authService.getCurrentUser();
            System.out.println("[ACCOUNTS] Creating " + accountType + " account for user: " + user.getUsername());
            
            Account.AccountType type;
            try {
                type = Account.AccountType.valueOf(accountType.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("[ACCOUNTS] Invalid account type: " + accountType);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid account type: " + accountType);
                return ResponseEntity.badRequest().body(error);
            }
            
            Account account = accountService.createAccount(user.getId(), type, accountName);
            System.out.println("[ACCOUNTS] Account created: " + account.getAccountNumber());
            
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            System.err.println("[ACCOUNTS] ERROR creating account: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        try {
            User user = authService.getCurrentUser();
            List<Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            boolean ownsAccount = userAccounts.stream()
                    .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            if (!ownsAccount) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account not found or access denied");
                return ResponseEntity.badRequest().body(error);
            }

            return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "balance", accountService.getAccountBalance(accountNumber)
            ));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        try {
            User user = authService.getCurrentUser();
            List<Account> userAccounts = accountService.getUserAccounts(user.getId());
            
            Account account = userAccounts.stream()
                    .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            return ResponseEntity.ok(account);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
