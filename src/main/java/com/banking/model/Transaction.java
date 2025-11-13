package com.banking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    @JsonIgnore
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    @JsonIgnore
    private Account toAccount;

    @Column(length = 20)
    private String externalAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String referenceNumber;
    private String remarks;

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
        if (transactionId == null) {
            transactionId = generateTransactionId();
        }
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, BILL_PAYMENT, RECHARGE, INTEREST
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }

    // Constructors
    public Transaction() {}

    public Transaction(Long id, String transactionId, Account fromAccount, Account toAccount,
                       String externalAccountNumber, BigDecimal amount, TransactionType transactionType,
                       TransactionStatus status, String description, LocalDateTime transactionDate,
                       String referenceNumber, String remarks) {
        this.id = id;
        this.transactionId = transactionId;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.externalAccountNumber = externalAccountNumber;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.description = description;
        this.transactionDate = transactionDate;
        this.referenceNumber = referenceNumber;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }
    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }
    public String getExternalAccountNumber() { return externalAccountNumber; }
    public void setExternalAccountNumber(String externalAccountNumber) { this.externalAccountNumber = externalAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    // Helper methods for JSON serialization (to expose account numbers)
    public String getFromAccountNumber() {
        return fromAccount != null ? fromAccount.getAccountNumber() : null;
    }
    
    public String getToAccountNumber() {
        return toAccount != null ? toAccount.getAccountNumber() : null;
    }
}

