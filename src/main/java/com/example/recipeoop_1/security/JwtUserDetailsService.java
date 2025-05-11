package com.example.recipeoop_1.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("jwtUserDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserInfo> users = new HashMap<>();

    @Autowired
    public JwtUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        // Initialize with admin and normal user
        this.users.put("admin", new UserInfo(
                "admin",
                passwordEncoder.encode("1234"),
                Arrays.asList("ROLE_ADMIN", "ROLE_USER")
        ));

        this.users.put("user", new UserInfo(
                "user",
                passwordEncoder.encode("user"),
                Collections.singletonList("ROLE_USER")
        ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = users.get(username);

        if (userInfo == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String role : userInfo.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return new User(userInfo.getUsername(), userInfo.getPassword(), authorities);
    }

    // Inner class to hold user information
    private static class UserInfo {
        private final String username;
        private final String password;
        private final List<String> roles;

        public UserInfo(String username, String password, List<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public List<String> getRoles() {
            return roles;
        }
    }
}