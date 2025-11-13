package com.banking.service;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createAccount(Long userId, Account.AccountType accountType, String accountName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accountNumber = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountName(accountName != null && !accountName.trim().isEmpty() ? accountName.trim() : null);
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setIfscCode("BANK0001234");

        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return accountRepository.findByUser(user);
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        return account.getBalance();
    }

    @Transactional
    public Account updateBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        Random random = new Random();
        long number = 1000000000L + random.nextInt(900000000);
        return String.valueOf(number);
    }
}

