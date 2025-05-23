package com.example.recipeoop_1.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; // Ensures the filter is executed only once per request

import java.io.IOException;

/**
 * A Spring Security filter that intercepts incoming HTTP requests to validate JWT tokens.
 * <p>
 * This filter extends {@link OncePerRequestFilter} to ensure it's executed once per request.
 * It checks for a JWT token in the "Authorization" header of the request. If a valid token
 * is found, it authenticates the user and sets the {@link org.springframework.security.core.Authentication}
 * object in the {@link SecurityContextHolder}. This allows subsequent parts of the application
 * (e.g., secured controller methods) to access the authenticated user's details.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 * @see OncePerRequestFilter
 * @see JwtTokenUtil
 * @see UserDetailsService
 * @see SecurityContextHolder
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private UserDetailsService userDetailsService;
    private JwtTokenUtil jwtTokenUtil;

    /**
     * Constructs a {@code JwtRequestFilter} with necessary dependencies.
     * <p>
     * The {@link UserDetailsService} is injected with {@link Lazy} annotation to help break
     * potential circular dependencies that can occur in Spring Security configurations,
     * particularly when {@code WebSecurityConfig} and {@code UserDetailsService} implementations
     * might reference each other indirectly.
     * </p>
     *
     * @param userDetailsService The service (qualified as "jwtUserDetailsService") used to load user-specific data.
     * @param jwtTokenUtil The utility class for JWT token generation and validation.
     */
    @Autowired
    public JwtRequestFilter(@Lazy @Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService,
                            JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Determines whether this filter should not be applied to the given request.
     * <p>
     * This implementation skips filtering for paths related to authentication (e.g., "/api/auth/")
     * and, currently, paths related to Swagger API documentation.
     * </p>
     * <p>
     * Note: If Swagger is being removed from the project, the paths
     * {@code /swagger-ui/}, {@code /api-docs/}, and {@code /v3/api-docs/}
     * should also be removed from this condition.
     * </p>
     *
     * @param request The current HTTP request.
     * @return {@code true} if the filter should not be applied, {@code false} otherwise.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Paths to exclude from JWT filtering
        return path.startsWith("/api/auth/") ||         // Authentication endpoint
                path.startsWith("/swagger-ui/") ||     // Swagger UI (Consider removal)
                path.startsWith("/api-docs/") ||       // Swagger API docs (Consider removal)
                path.startsWith("/v3/api-docs/");    // Swagger API docs v3 (Consider removal)
    }

    /**
     * Performs the core filtering logic for JWT validation and authentication.
     * <p>
     * This method is called for each request that is not excluded by {@link #shouldNotFilter}.
     * It attempts to extract a JWT token from the "Authorization" header. If a token is found,
     * it's validated. If valid, the user's details are loaded, and an authentication token
     * is created and set in the {@link SecurityContextHolder}.
     * </p>
     *
     * @param request The {@link HttpServletRequest}.
     * @param response The {@link HttpServletResponse}.
     * @param chain The {@link FilterChain} to pass the request and response to the next filter.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is expected in the format "Bearer token".
        // This block extracts the token if the header is present and correctly formatted.
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7); // Remove "Bearer " prefix
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                // Logged by the OncePerRequestFilter's logger (e.g., this.logger.warn)
                logger.warn("Unable to get JWT Token: Token format might be invalid or claims string is empty.");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired.");
            } catch (Exception e) {
                // Catching a broader range of JWT-related exceptions (e.g., malformed, signature issues)
                logger.warn("JWT Token validation error: " + e.getMessage());
            }
        } else {
            // Log if the Authorization header is missing or doesn't start with "Bearer "
            logger.debug("JWT Token does not begin with Bearer String or is not present in Authorization header.");
        }

        // After extracting the token and username, validate it.
        // This proceeds only if a username was successfully extracted from the token
        // AND there is no existing authentication in the current security context.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // If the token is valid, configure Spring Security to manually set authentication.
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    // Set details of the web request to the authentication token
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (Exception e) {
                // Log any error during user details loading or token validation after username extraction
                logger.error("Cannot set user authentication: " + e.getMessage(), e);
            }
        }

        // Continue the filter chain, passing the request and response to the next filter or target resource.
        chain.doFilter(request, response);
    }
}