package com.example.recipeoop_1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // For creating secure keys
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for performing JWT (JSON Web Token) operations.
 * <p>
 * This class provides methods for generating new JWTs, validating existing tokens,
 * and extracting claims (like username and expiration date) from tokens.
 * It uses a secret key and an expiration time configured in the application properties.
 * </p>
 * It implements {@link Serializable} as it's a common practice for components,
 * though not strictly required for its core JWT utility functions.
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see Component
 * @see Serializable
 */
@Component
public class JwtTokenUtil implements Serializable {

    /**
     * The serialization runtime uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object.
     */
    private static final long serialVersionUID = -2550185165626007488L;

    /**
     * The secret key used for signing and verifying JWT tokens.
     * This value is injected from the application property {@code jwt.secret}.
     * A default value is provided if the property is not set.
     * It is crucial that this secret is kept confidential and is strong.
     */
    @Value("${jwt.secret:6c0f0014f71deeacf37fa86a8d4eb27d5a9b7a234ec6f945c79bc57c24a32bb5c28f9e01a2e76c3fdfa76152c67b4f12}")
    private String secret;

    /**
     * The expiration time for JWT tokens, in seconds.
     * This value is injected from the application property {@code jwt.expiration}.
     * A default value (e.g., 86400 seconds = 24 hours) is provided if the property is not set.
     */
    @Value("${jwt.expiration:86400}") // Default to 24 hours in seconds
    private long expiration;

    /**
     * Generates a JWT token for a given user.
     * <p>
     * The token will include the username as the subject and will be signed using
     * the configured secret key and HS512 algorithm. It will also have an issue date
     * and an expiration date based on the configured {@code expiration} time.
     * </p>
     *
     * @param userDetails The {@link UserDetails} object representing the user for whom the token is to be generated.
     * The username from these details will be used as the subject of the token. Must not be {@code null}.
     * @return A string representing the generated JWT.
     * @see #doGenerateToken(Map, String)
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(); // No custom claims added by default in this implementation
        // Additional claims like roles could be added here if needed:
        // claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * Performs the actual JWT token generation with specified claims and subject.
     *
     * @param claims A map of claims to be included in the JWT payload.
     * @param subject The subject of the token, typically the username.
     * @return The compact, URL-safe JWT string.
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + expiration * 1000)) // expiration is in seconds
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates a given JWT token against the provided user details.
     * <p>
     * Validation involves checking if the username extracted from the token matches the
     * username in the {@link UserDetails} object and ensuring that the token has not expired.
     * </p>
     *
     * @param token The JWT token string to validate.
     * @param userDetails The {@link UserDetails} of the user against whom the token should be validated.
     * @return {@code true} if the token is valid for the given user and not expired, {@code false} otherwise.
     * @see #getUsernameFromToken(String)
     * @see #isTokenExpired(String)
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Retrieves the username (subject claim) from the given JWT token.
     *
     * @param token The JWT token string.
     * @return The username extracted from the token's subject claim.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed or is invalid.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieves the expiration date (expiration claim) from the given JWT token.
     *
     * @param token The JWT token string.
     * @return The {@link Date} object representing the token's expiration time.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed or is invalid.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * A generic method to retrieve a specific claim from a JWT token using a claims resolver function.
     *
     * @param <T> The type of the claim to be retrieved.
     * @param token The JWT token string.
     * @param claimsResolver A {@link Function} that takes {@link Claims} and returns the desired claim of type T.
     * @return The resolved claim value.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed or is invalid.
     * @see #getAllClaimsFromToken(String)
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT token and retrieves all claims contained within it.
     * <p>
     * This method uses the configured signing key to verify the token's signature before extracting claims.
     * </p>
     *
     * @param token The JWT token string.
     * @return A {@link Claims} object representing all claims from the token.
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired, or has an invalid signature.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token) // Verifies signature and parses claims
                .getBody();
    }

    /**
     * Checks if the given JWT token has expired.
     *
     * @param token The JWT token string.
     * @return {@code true} if the token's expiration date is before the current date, {@code false} otherwise.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed to retrieve the expiration date.
     */
    private Boolean isTokenExpired(String token) {
        final Date tokenExpirationDate = getExpirationDateFromToken(token);
        return tokenExpirationDate.before(new Date());
    }

    /**
     * Generates the signing key for JWT operations from the configured secret string.
     * <p>
     * The secret string is converted to bytes and then used to create a secure HMAC SHA key
     * suitable for HS512 algorithm.
     * </p>
     *
     * @return A {@link Key} object representing the signing key.
     */
    private Key getSigningKey() {
        // The secret key should be strong enough for the chosen algorithm (HS512 needs at least 512 bits / 64 bytes)
        // The provided default secret is a long hex string, which is good.
        byte[] keyBytes = secret.getBytes(); // Uses default charset, consider StandardCharsets.UTF_8
        return Keys.hmacShaKeyFor(keyBytes);
    }
}