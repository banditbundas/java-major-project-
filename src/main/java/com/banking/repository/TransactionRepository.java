package com.banking.repository;

import com.banking.model.Account;
import com.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountOrToAccount(Account fromAccount, Account toAccount);
    List<Transaction> findByFromAccount(Account account);
    List<Transaction> findByToAccount(Account account);
    List<Transaction> findByTransactionType(Transaction.TransactionType type);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountAndDateRange(@Param("account") Account account,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountOrderByDateDesc(@Param("account") Account account);
}

