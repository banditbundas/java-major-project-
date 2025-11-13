package com.banking.config;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public DataInitializer(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Create default accounts for all existing users who don't have any
        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<Account> existingAccounts = accountRepository.findByUser(user);
            if (existingAccounts.isEmpty()) {
                System.out.println("Creating default accounts for user: " + user.getUsername());
                
                // Create Savings Account
                Account savingsAccount = new Account();
                savingsAccount.setAccountNumber(generateAccountNumber());
                savingsAccount.setAccountName("My Savings Account");
                savingsAccount.setAccountType(Account.AccountType.SAVINGS);
                savingsAccount.setBalance(new BigDecimal("10000.00"));
                savingsAccount.setUser(user);
                savingsAccount.setIfscCode("BANK0001234");
                accountRepository.save(savingsAccount);
                
                // Create Current Account
                Account currentAccount = new Account();
                currentAccount.setAccountNumber(generateAccountNumber());
                currentAccount.setAccountName("My Current Account");
                currentAccount.setAccountType(Account.AccountType.CURRENT);
                currentAccount.setBalance(new BigDecimal("5000.00"));
                currentAccount.setUser(user);
                currentAccount.setIfscCode("BANK0001234");
                accountRepository.save(currentAccount);
                
                System.out.println("Created 2 default accounts for user: " + user.getUsername());
            }
        }
    }

    private String generateAccountNumber() {
        Random random = new Random();
        long number = 1000000000L + random.nextInt(900000000);
        String accountNumber = String.valueOf(number);
        
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            number = 1000000000L + random.nextInt(900000000);
            accountNumber = String.valueOf(number);
        }
        
        return accountNumber;
    }
}

