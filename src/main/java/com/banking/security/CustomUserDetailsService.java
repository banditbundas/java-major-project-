package com.banking.security;

import com.banking.model.User;
import com.banking.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }

    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String username;
        private String password;
        private String email;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean enabled;

        public UserPrincipal(Long id, String username, String password, String email,
                           Collection<? extends GrantedAuthority> authorities, boolean enabled) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.email = email;
            this.authorities = authorities;
            this.enabled = enabled;
        }

        public static UserPrincipal create(User user) {
            Collection<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            return new UserPrincipal(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    authorities,
                    user.isEnabled()
            );
        }

        public Long getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}

