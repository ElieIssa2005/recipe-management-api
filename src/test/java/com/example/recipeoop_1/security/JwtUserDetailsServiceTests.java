package com.example.recipeoop_1.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock; // Keep @Mock for PasswordEncoder
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link JwtUserDetailsService}.
 * Focuses on user loading logic and password encoding interaction.
 */
@ExtendWith(MockitoExtension.class)
class JwtUserDetailsServiceTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    // Remove @InjectMocks, we will instantiate manually
    private JwtUserDetailsService jwtUserDetailsService;

    @BeforeEach
    void setUp() {
        // Define mock behavior for PasswordEncoder FIRST
        when(passwordEncoder.encode("1234")).thenReturn("encodedAdminPassword");
        when(passwordEncoder.encode("user")).thenReturn("encodedUserPassword");

        // THEN instantiate the service, so its constructor uses the mocked encoder correctly
        jwtUserDetailsService = new JwtUserDetailsService(passwordEncoder);
    }

    @Test
    void testLoadUserByUsername_AdminUserFound() {
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails, "UserDetails should not be null for admin.");
        assertEquals("admin", userDetails.getUsername(), "Username should be 'admin'.");
        assertEquals("encodedAdminPassword", userDetails.getPassword(), "Password should be the mock encoded password for admin.");
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")),
                "User should have ROLE_ADMIN.");
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")),
                "User should have ROLE_USER.");
        assertEquals(2, userDetails.getAuthorities().size(), "Admin should have 2 authorities.");
    }

    @Test
    void testLoadUserByUsername_RegularUserFound() {
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername("user");

        assertNotNull(userDetails, "UserDetails should not be null for user.");
        assertEquals("user", userDetails.getUsername(), "Username should be 'user'.");
        assertEquals("encodedUserPassword", userDetails.getPassword(), "Password should be the mock encoded password for user.");
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")),
                "User should have ROLE_USER.");
        assertEquals(1, userDetails.getAuthorities().size(), "User should have 1 authority.");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String unknownUsername = "unknownUser";

        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            jwtUserDetailsService.loadUserByUsername(unknownUsername);
        }, "UsernameNotFoundException should be thrown for an unknown user.");

        assertEquals("User not found with username: " + unknownUsername, exception.getMessage(), "Exception message mismatch.");
        // The UnnecessaryStubbingException might occur if this test doesn't trigger
        // the passwordEncoder.encode() calls. Since the constructor does, the stubs in setUp are necessary.
        // If this specific test still shows it, it's usually benign if other tests pass and need those stubs.
    }

    @Test
    void testConstructor_InitializesUsersWithEncodedPasswords() {
        // This test is implicitly covered by the above tests after the fix.
        // It verifies that the constructor, when called in setUp, used the mocked encoder.
        UserDetails adminDetails = jwtUserDetailsService.loadUserByUsername("admin");
        assertEquals("encodedAdminPassword", adminDetails.getPassword(), "Admin password should reflect mocked encoding from constructor.");

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername("user");
        assertEquals("encodedUserPassword", userDetails.getPassword(), "User password should reflect mocked encoding from constructor.");
    }
}