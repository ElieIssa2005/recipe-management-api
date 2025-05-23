package com.example.recipeoop_1.controller;

import com.example.recipeoop_1.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling user authentication.
 * <p>
 * This controller provides an endpoint for users to log in by submitting their
 * credentials (username and password). Upon successful authentication, it returns a
 * JSON Web Token (JWT) that can be used to access protected resources.
 * The base path for authentication endpoints is {@code /api/auth}.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.2
 * @since 2025-05-17
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Manages the authentication process.
     */
    private final AuthenticationManager authenticationManager;
    /**
     * Utility for JWT token operations.
     */
    private final JwtTokenUtil jwtTokenUtil;
    /**
     * Service for loading user-specific data.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Constructs an {@code AuthController} with necessary dependencies for authentication.
     *
     * @param authenticationManager The Spring Security {@link AuthenticationManager} for processing authentication requests.
     * @param jwtTokenUtil Utility class ({@link JwtTokenUtil}) for generating and validating JWT tokens.
     * @param userDetailsService Service ({@link UserDetailsService}) for loading user-specific data,
     * qualified with "jwtUserDetailsService" to specify the bean.
     */
    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenUtil jwtTokenUtil,
                          @Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Authenticates a user and returns a JWT token upon successful authentication.
     * <p>
     * This endpoint expects a POST request to {@code /api/auth/login} with a JSON body
     * containing the user's username and password.
     * </p>
     * HTTP Method: POST
     * Path: /api/auth/login
     * <p>
     * Request Body:
     * <pre>{@code
     * {
     * "username": "user_example",
     * "password": "password_example"
     * }
     * }</pre>
     * (See {@link JwtRequest} for details on the request structure)
     * <p>
     * Response:
     * <ul>
     * <li>200 OK: Authentication successful. Returns a {@link JwtResponse} containing the JWT token.
     * <pre>{@code
     * {
     * "token": "generated_jwt_token_string"
     * }
     * }</pre>
     * </li>
     * <li>401 Unauthorized: If the user is disabled ({@link DisabledException}) or if credentials
     * are invalid ({@link BadCredentialsException}). The response body will contain a specific error message.</li>
     * <li>500 Internal Server Error: If any other unexpected error occurs during authentication.
     * The response body will contain a generic error message.</li>
     * </ul>
     *
     * @param authenticationRequest A {@link JwtRequest} object containing the username and password from the request body.
     * @return A {@link ResponseEntity} containing a {@link JwtResponse} with the token on success,
     * or an error message and appropriate HTTP status on failure.
     * @see JwtRequest
     * @see JwtResponse
     * @see JwtTokenUtil#generateToken(UserDetails)
     * @see UserDetailsService#loadUserByUsername(String)
     * @see AuthenticationManager#authenticate(org.springframework.security.core.Authentication)
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());

            final String token = jwtTokenUtil.generateToken(userDetails);

            return ResponseEntity.ok(new JwtResponse(token));
        } catch (DisabledException e) {
            return ResponseEntity.status(401).body("User is disabled");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Authentication error: " + e.getMessage());
        }
    }

    /**
     * Represents the request body for JWT authentication.
     */
    public static class JwtRequest {
        /**
         * The username for authentication.
         */
        private String username;
        /**
         * The password for authentication.
         */
        private String password;

        /**
         * Default constructor for {@link JwtRequest}.
         */
        public JwtRequest() {
        }

        /**
         * Constructs a {@link JwtRequest} with the specified username and password.
         *
         * @param username The username of the user attempting to authenticate.
         * @param password The password of the user attempting to authenticate.
         */
        public JwtRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Gets the username from the authentication request.
         * @return The username.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets the username for the authentication request.
         * @param username The username to set.
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Gets the password from the authentication request.
         * @return The password.
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the password for the authentication request.
         * @param password The password to set.
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Represents the response body containing the JWT token after successful authentication.
     */
    public static class JwtResponse {
        /**
         * The JWT token string.
         */
        private final String jwtToken;

        /**
         * Constructs a {@link JwtResponse} with the generated JWT token.
         * @param jwtToken The JWT token string.
         */
        public JwtResponse(String jwtToken) {
            this.jwtToken = jwtToken;
        }

        /**
         * Gets the JWT token.
         * @return The JWT token string.
         */
        public String getToken() {
            return this.jwtToken;
        }
    }
}