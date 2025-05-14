package com.example.recipeoop_1.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration class for the application.
 * <p>
 * This class enables web security features and method-level security. It defines
 * the security filter chain that handles HTTP security, including CSRF protection,
 * request authorization, session management, and JWT token processing.
 * </p>
 * Annotations used:
 * <ul>
 * <li>{@link Configuration}: Indicates that this class contains Spring bean definitions.</li>
 * <li>{@link EnableWebSecurity}: Enables Spring Security's web security support and provides
 * Spring MVC integration.</li>
 * <li>{@link EnableMethodSecurity}: Enables method-level security annotations like
 * {@code @PreAuthorize} on controller methods.</li>
 * </ul>
 *
 * @author Your Name/Team Name
 * @version 1.1
 * @since 2025-05-14
 * @see HttpSecurity
 * @see SecurityFilterChain
 * @see JwtAuthenticationEntryPoint
 * @see JwtRequestFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc. on methods
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Constructs the {@code WebSecurityConfig} with necessary JWT components.
     *
     * @param jwtAuthenticationEntryPoint The entry point to commence authentication when an
     * {@link org.springframework.security.core.AuthenticationException} is thrown.
     * @param jwtRequestFilter            The filter that processes JWT tokens from incoming requests.
     */
    @Autowired
    public WebSecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                             JwtRequestFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Configures the main {@link SecurityFilterChain} bean that defines the security rules for HTTP requests.
     * <p>
     * The configuration includes:
     * <ul>
     * <li>Disabling CSRF (Cross-Site Request Forgery) protection, which is common for stateless REST APIs.</li>
     * <li>Defining authorization rules for various request matchers:
     * <ul>
     * <li>Permitting public access to static frontend content (root, HTML files), static resources (CSS, JS),
     * the health check endpoint ("/health"), Javadoc documentation ("/apidocs/**"),
     * and authentication endpoints ("/api/auth/**").</li>
     * <li>Restricting access to admin-specific recipe endpoints ("/api/recipes/admin/**") to users with the "ADMIN" role.</li>
     * <li>Requiring "USER" or "ADMIN" roles for general recipe API endpoints ("/api/recipes/**").</li>
     * <li>Requiring authentication for any other request not explicitly matched.</li>
     * </ul>
     * </li>
     * <li>Setting up exception handling to use the {@link JwtAuthenticationEntryPoint} for authentication failures.</li>
     * <li>Configuring session management to be stateless, as JWTs are used for session handling.</li>
     * <li>Adding the {@link JwtRequestFilter} before the standard {@link UsernamePasswordAuthenticationFilter}
     * to process JWTs in requests.</li>
     * </ul>
     *
     * @param http The {@link HttpSecurity} object to configure.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection, as it's generally not needed for stateless REST APIs using tokens.
                .csrf(csrf -> csrf.disable())
                // Configure authorization rules for HTTP requests.
                .authorizeHttpRequests(authorize -> authorize
                        // Publicly accessible endpoints:
                        .requestMatchers("/", "/index.html", "/customer.html", "/chef.html").permitAll() // Old static frontend pages (consider if still needed)
                        .requestMatchers("/apidocs/**").permitAll() // Javadoc documentation
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll() // Static resources
                        .requestMatchers("/health").permitAll() // Health check endpoint
                        .requestMatchers("/api/auth/**").permitAll() // Authentication endpoints (e.g., login)
                        // Swagger/OpenAPI paths - ensure these are removed if Swagger is fully deprecated
                        // .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Protected API endpoints:
                        .requestMatchers("/api/recipes/admin/**").hasRole("ADMIN") // Admin-specific recipe operations
                        .requestMatchers("/api/recipes/**").hasAnyRole("USER", "ADMIN") // General recipe operations

                        // All other requests must be authenticated.
                        .anyRequest().authenticated()
                )
                // Configure exception handling, specifically for authentication errors.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Handles failed authentication attempts
                )
                // Configure session management.
                .sessionManagement(session -> session
                        // Set session creation policy to STATELESS, as JWTs are used for session management.
                        // The server will not create or use HTTP sessions.
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // Add the custom JWT request filter before the standard username/password authentication filter.
        // This ensures JWT tokens are processed for authentication before other mechanisms.
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}