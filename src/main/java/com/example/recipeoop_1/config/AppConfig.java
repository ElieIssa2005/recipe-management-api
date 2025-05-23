package com.example.recipeoop_1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * General application configuration class.
 * <p>
 * This class provides bean definitions for core components like password encoding
 * and authentication management that are used throughout the application.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 */
@Configuration
public class AppConfig {

    /**
     * Provides a {@link PasswordEncoder} bean that uses BCrypt hashing algorithm.
     * <p>
     * BCrypt is a strong, adaptive hashing algorithm recommended for password storage.
     * This bean will be used by Spring Security to encode and validate passwords.
     * </p>
     *
     * @return A {@link BCryptPasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides the {@link AuthenticationManager} bean.
     * <p>
     * The AuthenticationManager is a core part of Spring Security's authentication mechanism.
     * It processes an {@code Authentication} request. This bean is obtained from the
     * {@link AuthenticationConfiguration}.
     * </p>
     *
     * @param authConfig The Spring Security {@link AuthenticationConfiguration} used to obtain the AuthenticationManager.
     * @return The configured {@link AuthenticationManager}.
     * @throws Exception if an error occurs while retrieving the AuthenticationManager from the configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}