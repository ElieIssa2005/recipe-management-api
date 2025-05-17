package com.example.recipeoop_1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
// MockitoExtension is sufficient, no need for @Mock if not mocking dependencies of JwtTokenUtil itself
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link JwtTokenUtil}.
 * Focuses on token generation, validation, and claim extraction logic.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTests {

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private final String testSecret = "testSecretKeyForJwtTokenUtilUnitTestsRecipeAppRecipeManagementSystem12345"; // 64 chars
    private final Long testExpiration = 3600L; // 1 hour in seconds
    private final Long shortExpiration = 1L; // 1 second for testing expiration

    private UserDetails mockUserDetails;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        mockUserDetails = new User("testUser", "password", authorities);
        signingKey = Keys.hmacShaKeyFor(testSecret.getBytes());
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsernameFromToken_Success() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        String usernameFromToken = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals(mockUserDetails.getUsername(), usernameFromToken);
    }

    @Test
    void testGetExpirationDateFromToken_Success() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);
        Date issueDate = jwtTokenUtil.getClaimFromToken(token, Claims::getIssuedAt);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(issueDate));
        long diffInMillis = expirationDate.getTime() - issueDate.getTime();
        long diffInSeconds = diffInMillis / 1000;
        assertEquals(testExpiration, diffInSeconds, 1);
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        Boolean isValid = jwtTokenUtil.validateToken(token, mockUserDetails);
        assertTrue(isValid);
    }

    /**
     * Tests {@link JwtTokenUtil#validateToken(String, UserDetails)} with an expired token.
     * Verifies that an ExpiredJwtException is thrown by validateToken, as it calls
     * getUsernameFromToken which will throw this exception for an expired token.
     */
    @Test
    void testValidateToken_ExpiredToken_ThrowsException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", shortExpiration); // 1 second
        String token = jwtTokenUtil.generateToken(mockUserDetails);

        Thread.sleep((shortExpiration * 1000) + 500); // Wait for 1.5 seconds for token to expire

        // Act & Assert: Expect ExpiredJwtException when validateToken is called.
        assertThrows(ExpiredJwtException.class, () -> {
            jwtTokenUtil.validateToken(token, mockUserDetails);
        }, "validateToken should throw ExpiredJwtException for an expired token.");
    }

    /**
     * Tests the private method isTokenExpired via reflection for an expired token.
     * It should throw ExpiredJwtException because getExpirationDateFromToken (called within isTokenExpired)
     * will throw it when trying to parse an expired token.
     */
    @Test
    void testIsTokenExpired_ViaReflection_ExpiredToken_ThrowsException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", shortExpiration); // 1 second
        String token = jwtTokenUtil.generateToken(mockUserDetails);

        Thread.sleep((shortExpiration * 1000) + 500); // Wait for token to expire

        // Act & Assert: isTokenExpired calls getExpirationDateFromToken, which will throw ExpiredJwtException.
        assertThrows(ExpiredJwtException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtTokenUtil, "isTokenExpired", token);
        }, "Calling private isTokenExpired via reflection on an expired token should lead to ExpiredJwtException from getExpirationDateFromToken.");
    }


    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        UserDetails otherUserDetails = new User("otherUser", "password", Collections.emptyList());
        Boolean isValid = jwtTokenUtil.validateToken(token, otherUserDetails);
        assertFalse(isValid);
    }

    /**
     * Tests the private method {@code isTokenExpired(String)} for a non-expired token using ReflectionTestUtils.
     * This method should return Boolean.FALSE for a valid, non-expired token.
     * The line you mentioned (around 187) should use this reflection approach.
     */
    @Test
    void testIsTokenExpired_ViaReflection_NotExpired() {
        // Arrange: Generate a standard token with the default (longer) expiration.
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);
        String token = jwtTokenUtil.generateToken(mockUserDetails);

        // Act & Assert: Check if the token is expired using reflection. It should not be.
        // The private method isTokenExpired returns a Boolean.
        Boolean isExpired = ReflectionTestUtils.invokeMethod(jwtTokenUtil, "isTokenExpired", token);

        assertNotNull(isExpired, "The result of isTokenExpired should not be null.");
        assertFalse(isExpired, "Token should not be expired.");
    }

    @Test
    void testGenerateToken_WithClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        claims.put("userId", 123);

        long currentTimeMillis = System.currentTimeMillis();
        String tokenWithClaims = Jwts.builder()
                .setClaims(claims)
                .setSubject(mockUserDetails.getUsername())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + testExpiration * 1000))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        Claims extractedClaims = jwtTokenUtil.getClaimFromToken(tokenWithClaims, Function.identity());

        assertEquals("customValue", extractedClaims.get("customClaim", String.class));
        assertEquals(123, extractedClaims.get("userId", Integer.class));
        assertEquals(mockUserDetails.getUsername(), extractedClaims.getSubject());
    }

    @Test
    void testParseToken_InvalidSignature() {
        String token = jwtTokenUtil.generateToken(mockUserDetails);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            jwtTokenUtil.getUsernameFromToken(tamperedToken);
        });
    }

    @Test
    void testParseToken_MalformedToken() {
        String malformedToken = "this.is.not.a.valid.jwt.token";
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtTokenUtil.getUsernameFromToken(malformedToken);
        });
    }
}