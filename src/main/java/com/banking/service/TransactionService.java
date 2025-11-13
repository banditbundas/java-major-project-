package com.banking.service;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final XmlTransactionService xmlTransactionService;

    public TransactionService(TransactionRepository transactionRepository,
                             AccountService accountService,
                             XmlTransactionService xmlTransactionService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.xmlTransactionService = xmlTransactionService;
    }

    @Transactional
    public Transaction transferFunds(String fromAccountNumber, String toAccountNumber,
                                     BigDecimal amount, String description) {
        Account fromAccount = accountService.getAccountByNumber(fromAccountNumber);
        Account toAccount = accountService.getAccountByNumber(toAccountNumber);

        // Validate sufficient balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Validate accounts are different
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(description != null ? description : "Fund transfer");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setTransactionDate(LocalDateTime.now());

        try {
            // Log before update
            System.out.println("[TRANSFER] Before update - From Account: " + fromAccount.getAccountNumber() + " Balance: " + fromAccount.getBalance());
            System.out.println("[TRANSFER] Before update - To Account: " + toAccount.getAccountNumber() + " Balance: " + toAccount.getBalance());
            
            // Update balances
            Account updatedFromAccount = accountService.updateBalance(fromAccount, amount.negate());
            Account updatedToAccount = accountService.updateBalance(toAccount, amount);
            
            // Log after update
            System.out.println("[TRANSFER] After update - From Account: " + updatedFromAccount.getAccountNumber() + " Balance: " + updatedFromAccount.getBalance());
            System.out.println("[TRANSFER] After update - To Account: " + updatedToAccount.getAccountNumber() + " Balance: " + updatedToAccount.getBalance());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);
            
            System.out.println("[TRANSFER] Transaction saved with ID: " + transaction.getId());

            // Save to XML
            xmlTransactionService.saveTransactionToXml(transaction);

            return transaction;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setRemarks("Transaction failed: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed: " + e.getMessage());
        }
    }

    @Transactional
    public Transaction transferToExternalAccount(String fromAccountNumber, String externalAccountNumber,
                                                 String ifscCode, BigDecimal amount, String description) {
        Account fromAccount = accountService.getAccountByNumber(fromAccountNumber);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setExternalAccountNumber(externalAccountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(description != null ? description : "External transfer");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setRemarks("IFSC: " + ifscCode);

        try {
            accountService.updateBalance(fromAccount, amount.negate());
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);

            xmlTransactionService.saveTransactionToXml(transaction);
            return transaction;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setRemarks("Transaction failed: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transaction failed: " + e.getMessage());
        }
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount, String description) {
        Account account = accountService.getAccountByNumber(accountNumber);

        Transaction transaction = new Transaction();
        transaction.setToAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(description != null ? description : "Deposit");
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());

        accountService.updateBalance(account, amount);
        transaction = transactionRepository.save(transaction);
        xmlTransactionService.saveTransactionToXml(transaction);

        return transaction;
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount, String description) {
        Account account = accountService.getAccountByNumber(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Transaction transaction = new Transaction();
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(description != null ? description : "Withdrawal");
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setTransactionDate(LocalDateTime.now());

        try {
            accountService.updateBalance(account, amount.negate());
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction = transactionRepository.save(transaction);
            xmlTransactionService.saveTransactionToXml(transaction);
            return transaction;
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setRemarks("Withdrawal failed: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Withdrawal failed: " + e.getMessage());
        }
    }

    public List<Transaction> getAccountTransactions(String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return transactionRepository.findByAccountOrderByDateDesc(account);
    }

    public List<Transaction> getAccountTransactionsByDateRange(String accountNumber,
                                                               LocalDateTime startDate,
                                                               LocalDateTime endDate) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return transactionRepository.findByAccountAndDateRange(account, startDate, endDate);
    }

    public Transaction getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}

