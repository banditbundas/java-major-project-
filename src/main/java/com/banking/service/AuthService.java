package com.banking.service;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                      PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, 
                      JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public User registerUser(String username, String password, String email, String firstName,
                            String lastName, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setEnabled(true);
        user.setRole(User.UserRole.CUSTOMER);

        User savedUser = userRepository.save(user);
        
        // Create 2 default accounts for the new user
        createDefaultAccounts(savedUser);
        
        return savedUser;
    }
    
    private void createDefaultAccounts(User user) {
        // Create Savings Account
        Account savingsAccount = new Account();
        savingsAccount.setAccountNumber(generateAccountNumber());
        savingsAccount.setAccountName("My Savings Account");
        savingsAccount.setAccountType(Account.AccountType.SAVINGS);
        savingsAccount.setBalance(new BigDecimal("10000.00")); // Initial balance
        savingsAccount.setUser(user);
        savingsAccount.setIfscCode("BANK0001234");
        accountRepository.save(savingsAccount);
        
        // Create Current Account
        Account currentAccount = new Account();
        currentAccount.setAccountNumber(generateAccountNumber());
        currentAccount.setAccountName("My Current Account");
        currentAccount.setAccountType(Account.AccountType.CURRENT);
        currentAccount.setBalance(new BigDecimal("5000.00")); // Initial balance
        currentAccount.setUser(user);
        currentAccount.setIfscCode("BANK0001234");
        accountRepository.save(currentAccount);
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        long number = 1000000000L + random.nextInt(900000000);
        String accountNumber = String.valueOf(number);
        
        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            number = 1000000000L + random.nextInt(900000000);
            accountNumber = String.valueOf(number);
        }
        
        return accountNumber;
    }

    public Map<String, Object> login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("id", user.getId());

        return response;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

