package com.banking.controller;

import com.banking.model.Transaction;
import com.banking.service.TransactionService;
import com.banking.service.XmlTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final TransactionService transactionService;
    private final XmlTransactionService xmlTransactionService;

    public AdminController(TransactionService transactionService,
                          XmlTransactionService xmlTransactionService) {
        this.transactionService = transactionService;
        this.xmlTransactionService = xmlTransactionService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // This would require a UserService with getAllUsers method
        // For now, returning a placeholder
        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin endpoint - requires UserService implementation");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        List<XmlTransactionService.TransactionXml> transactions = xmlTransactionService.loadTransactionsFromXml();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/xml")
    public ResponseEntity<?> getTransactionsXml() {
        List<XmlTransactionService.TransactionXml> transactions = xmlTransactionService.loadTransactionsFromXml();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/accounts/{accountNumber}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String accountNumber,
                                     @RequestParam BigDecimal amount,
                                     @RequestParam(required = false) String description) {
        try {
            Transaction transaction = transactionService.deposit(accountNumber, amount, description);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

