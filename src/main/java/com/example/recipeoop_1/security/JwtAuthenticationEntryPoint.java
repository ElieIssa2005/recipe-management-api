package com.example.recipeoop_1.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

/**
 * Custom implementation of {@link AuthenticationEntryPoint} for handling JWT authentication failures.
 * <p>
 * This class is invoked when an unauthenticated user attempts to access a secured REST endpoint
 * that requires JWT authentication. Instead of redirecting to a login page (common in traditional
 * web applications), it returns an HTTP 401 Unauthorized error response directly to the client.
 * This is suitable for RESTful APIs where clients expect HTTP status codes to indicate
 * authentication issues.
 * </p>
 * It implements {@link Serializable} as it's good practice for components used in a web environment,
 * though not strictly necessary for its function as an entry point.
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see AuthenticationEntryPoint
 * @see Component
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    /**
     * The serialization runtime uses this number to ensure that a loaded class
     * corresponds exactly to a serialized object.
     * If you add or remove fields, you should change this number.
     */
    private static final long serialVersionUID = -7858869558953243875L;

    /**
     * This method is called whenever an exception is thrown due to an unauthenticated user
     * trying to access a resource that requires authentication.
     * <p>
     * It sends an HTTP 401 Unauthorized error response to the client, indicating that
     * authentication is required and has failed or has not yet been provided.
     * </p>
     *
     * @param request The {@link HttpServletRequest} that resulted in an {@code AuthenticationException}.
     * @param response The {@link HttpServletResponse} so that the error can be sent to the client.
     * @param authException The {@link AuthenticationException} that caused the invocation.
     * @throws IOException if an input or output exception occurs during the response sending.
     * (Though {@code sendError} typically handles its own IO exceptions internally).
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This sends a 401 Unauthorized error to the client
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}