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

/**
 * Service implementation for {@link UserDetailsService} to load user-specific data.
 * <p>
 * This service is used by Spring Security to retrieve user details (username, password, authorities)
 * during the authentication process, particularly for JWT-based authentication.
 * In this implementation, user details are stored in-memory in a {@link HashMap}.
 * It pre-populates two users: "admin" (with ADMIN and USER roles) and "user" (with USER role).
 * Passwords for these users are encoded using the provided {@link PasswordEncoder}.
 * </p>
 * The bean is named "jwtUserDetailsService" and can be injected using this qualifier.
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.2
 * @since 2025-05-14
 * @see UserDetailsService
 * @see UserDetails
 * @see User
 * @see PasswordEncoder
 * @see Service
 */
@Service("jwtUserDetailsService")
public class JwtUserDetailsService implements UserDetailsService {

    /**
     * The {@link PasswordEncoder} used for encoding passwords.
     * This is typically injected by Spring.
     */
    private final PasswordEncoder passwordEncoder;
    /**
     * In-memory storage for user information. The key is the username,
     * and the value is a {@link UserInfo} object containing the hashed password and roles.
     */
    private final Map<String, UserInfo> users = new HashMap<>();

    /**
     * Constructs the {@code JwtUserDetailsService} and initializes in-memory users.
     * <p>
     * It takes a {@link PasswordEncoder} to securely store hashed passwords for the predefined users.
     * Two users are created by default:
     * </p>
     * <ul>
     * <li><b>admin</b>: Password "1234", Roles "ROLE_ADMIN", "ROLE_USER"</li>
     * <li><b>user</b>: Password "user", Roles "ROLE_USER"</li>
     * </ul>
     *
     * @param passwordEncoder The {@link PasswordEncoder} used for encoding passwords.
     */
    @Autowired
    public JwtUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

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

    /**
     * Locates the user based on the username.
     * <p>
     * This method is called by Spring Security's authentication mechanism.
     * It retrieves user information from the in-memory {@code users} map.
     * If the user is found, it constructs and returns a {@link UserDetails} object
     * (specifically, a {@link org.springframework.security.core.userdetails.User})
     * containing the username, hashed password, and granted authorities (roles).
     * </p>
     *
     * @param username The username identifying the user whose data is required.
     * @return A fully populated {@link UserDetails} object (never {@code null}).
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     * GrantedAuthority. This exception is part of the {@link UserDetailsService} contract.
     */
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

    /**
     * Inner static class to hold user information (username, password, roles)
     * for the in-memory user store.
     * <p>
     * This class encapsulates the details for each user managed by {@link JwtUserDetailsService}.
     * </p>
     */
    private static class UserInfo {
        /** The username of the user. */
        private final String username;
        /** The encoded password of the user. */
        private final String password;
        /** The list of roles assigned to the user. */
        private final List<String> roles;

        /**
         * Constructs a {@code UserInfo} object.
         *
         * @param username The username.
         * @param password The encoded password.
         * @param roles A list of roles assigned to the user (e.g., "ROLE_USER", "ROLE_ADMIN").
         */
        public UserInfo(String username, String password, List<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }

        /**
         * Gets the username.
         * @return The username.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Gets the encoded password.
         * @return The encoded password string.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Gets the list of roles assigned to the user.
         * @return A list of role strings.
         */
        public List<String> getRoles() {
            return roles;
        }
    }
}