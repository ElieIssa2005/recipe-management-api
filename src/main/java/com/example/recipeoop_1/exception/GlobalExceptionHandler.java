package com.example.recipeoop_1.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode; // Import for handleMethodArgumentNotValid
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable; // Import for handleMethodArgumentNotValid
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * <p>
 * This class uses {@link ControllerAdvice} to centralize exception handling logic
 * across all controllers. It extends {@link ResponseEntityExceptionHandler} to provide
 * handling for common Spring MVC exceptions and adds custom handlers for application-specific
 * exceptions like {@link RecipeNotFoundException} and a generic handler for any other unhandled exceptions.
 * It ensures that API clients receive consistent and informative error responses in a JSON format.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see ControllerAdvice
 * @see ResponseEntityExceptionHandler
 * @see RecipeNotFoundException
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link RecipeNotFoundException} specifically.
     * <p>
     * This method is invoked when a {@link RecipeNotFoundException} is thrown anywhere
     * in the application (typically from service layers when a recipe cannot be found).
     * It returns a 404 Not Found HTTP status with a standardized {@link ErrorDetails} body.
     * </p>
     * Response Body Example:
     * <pre>{@code
     * {
     * "timestamp": "2025-05-14T12:00:00.000+00:00",
     * "message": "Recipe not found with id: 123",
     * "details": "uri=/api/recipes/123"
     * }
     * }</pre>
     *
     * @param exception The {@link RecipeNotFoundException} instance that was thrown.
     * @param request The current {@link WebRequest} providing context about the request.
     * @return A {@link ResponseEntity} containing {@link ErrorDetails} and HTTP status 404 (Not Found).
     */
    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleRecipeNotFoundException(
            RecipeNotFoundException exception, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                exception.getMessage(),
                request.getDescription(false)); // false to get only the path

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles all other unCaught {@link Exception} instances as a fallback.
     * <p>
     * This generic handler catches any exception that is not specifically handled
     * by other {@code @ExceptionHandler} methods within this class or other controller advices.
     * It returns a 500 Internal Server Error HTTP status with a standardized {@link ErrorDetails} body.
     * It's crucial for preventing unhandled exceptions from exposing stack traces or sensitive information.
     * </p>
     * Response Body Example:
     * <pre>{@code
     * {
     * "timestamp": "2025-05-14T12:05:00.000+00:00",
     * "message": "An unexpected error occurred",
     * "details": "uri=/some/problematic/endpoint"
     * }
     * }</pre>
     *
     * @param exception The {@link Exception} instance that was thrown.
     * @param request The current {@link WebRequest} providing context about the request.
     * @return A {@link ResponseEntity} containing {@link ErrorDetails} and HTTP status 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception exception, WebRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                exception.getMessage(), // Or a more generic message for production
                request.getDescription(false));

        // It's good practice to log the full exception server-side for debugging
        logger.error("Unhandled exception occurred: " + exception.getMessage(), exception);


        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link MethodArgumentNotValidException}.
     * <p>
     * This exception is thrown when an argument annotated with {@code @Valid} fails validation
     * (e.g., due to {@code @NotBlank}, {@code @Min} constraints in DTOs).
     * This overridden method customizes the response to provide a detailed map of validation errors.
     * It returns a 400 Bad Request HTTP status with a {@link ValidationErrorDetails} body.
     * </p>
     * Response Body Example:
     * <pre>{@code
     * {
     * "timestamp": "2025-05-14T12:10:00.000+00:00",
     * "message": "Validation Failed",
     * "details": "uri=/api/recipes",
     * "errors": {
     * "title": "Title is required",
     * "cookingTime": "Cooking time must be at least 1 minute"
     * }
     * }
     * }</pre>
     *
     * @param ex The {@link MethodArgumentNotValidException} instance containing validation errors.
     * @param headers The HTTP headers from the original request.
     * @param status The HTTP status determined by Spring (usually Bad Request).
     * @param request The current {@link WebRequest}.
     * @return A {@link ResponseEntity} containing {@link ValidationErrorDetails} and HTTP status 400 (Bad Request).
     */
    @Override // This method overrides one from ResponseEntityExceptionHandler
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers, // Added @Nullable as per Spring 6
            @Nullable HttpStatusCode status, // Changed from HttpStatus to HttpStatusCode as per Spring 6
            @Nullable WebRequest request) { // Added @Nullable as per Spring 6

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String requestDetails = (request != null) ? request.getDescription(false) : "N/A";

        ValidationErrorDetails errorDetails = new ValidationErrorDetails(
                new Date(),
                "Validation Failed",
                requestDetails,
                errors);

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST); // Or use the passed 'status'
    }

    /**
     * A generic structure for providing details about an error that occurred.
     * This class is used as the response body for most error responses.
     */
    public static class ErrorDetails {
        private Date timestamp;
        private String message;
        private String details;

        /**
         * Constructs an {@code ErrorDetails} object.
         *
         * @param timestamp The date and time when the error occurred.
         * @param message A summary message describing the error.
         * @param details Specific details about the error, often the request URI.
         */
        public ErrorDetails(Date timestamp, String message, String details) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }

        /**
         * Gets the timestamp of when the error occurred.
         *
         * @return The error timestamp.
         */
        public Date getTimestamp() {
            return timestamp;
        }

        /**
         * Gets the summary error message.
         *
         * @return The error message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets specific details about the error, typically the request URI.
         *
         * @return The error details.
         */
        public String getDetails() {
            return details;
        }
    }

    /**
     * An extension of {@link ErrorDetails} specifically for validation errors.
     * This class includes a map of field-specific error messages.
     *
     * @see ErrorDetails
     */
    public static class ValidationErrorDetails extends ErrorDetails {
        private Map<String, String> errors;

        /**
         * Constructs a {@code ValidationErrorDetails} object.
         *
         * @param timestamp The date and time when the validation error occurred.
         * @param message A summary message, typically "Validation Failed".
         * @param details Specific details about the request, often the request URI.
         * @param errors A map where keys are field names and values are the corresponding validation error messages.
         */
        public ValidationErrorDetails(Date timestamp, String message, String details,
                                      Map<String, String> errors) {
            super(timestamp, message, details);
            this.errors = errors;
        }

        /**
         * Gets the map of field-specific validation errors.
         *
         * @return A map of validation errors.
         */
        public Map<String, String> getErrors() {
            return errors;
        }
    }
}