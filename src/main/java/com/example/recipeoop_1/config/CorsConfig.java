package com.example.recipeoop_1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for Cross-Origin Resource Sharing (CORS).
 * <p>
 * This class defines the global CORS policy for the application, allowing
 * requests from any origin and specifying permitted HTTP methods and headers.
 * This is particularly useful when the frontend and backend are served from different domains.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 */
@Configuration
public class CorsConfig {

    /**
     * Configures CORS mappings for the application.
     * <p>
     * This bean defines a {@link WebMvcConfigurer} that customizes the CORS settings.
     * It allows all origins ("*"), common HTTP methods (GET, POST, PUT, DELETE, OPTIONS),
     * and all headers for all endpoints ("/**").
     * </p>
     * <p>
     * Note: For production environments, it's generally recommended to specify allowed origins
     * more restrictively rather than using "*".
     * </p>
     *
     * @return A {@link WebMvcConfigurer} instance with the defined CORS configuration.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Adds CORS mappings to the provided {@link CorsRegistry}.
             *
             * @param registry The {@link CorsRegistry} to which CORS mappings will be added.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Applies to all paths
                        .allowedOrigins("*") // Allows all origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specifies allowed HTTP methods
                        .allowedHeaders("*"); // Allows all headers
            }
        };
    }
}