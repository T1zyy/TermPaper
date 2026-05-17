package com.linktalk.service;

import com.linktalk.model.AuthUserDetails;
import com.linktalk.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(user -> new AuthUserDetails(user.getId(), user.getEmail(), user.getPasswordHash()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
