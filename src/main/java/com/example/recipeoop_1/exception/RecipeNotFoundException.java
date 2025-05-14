package com.example.recipeoop_1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom runtime exception thrown when a requested recipe cannot be found in the system.
 * <p>
 * This exception is annotated with {@link ResponseStatus}({@link HttpStatus#NOT_FOUND}),
 * which allows Spring MVC to automatically translate this exception into an HTTP 404 Not Found
 * response if it's not handled by a more specific {@code @ExceptionHandler} in a
 * {@code @ControllerAdvice} class (like {@link GlobalExceptionHandler}).
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see GlobalExceptionHandler#handleRecipeNotFoundException(RecipeNotFoundException, org.springframework.web.context.request.WebRequest)
 * @see RuntimeException
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Maps this exception to HTTP 404 Not Found
public class RecipeNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code RecipeNotFoundException} with the specified detail message.
     * <p>
     * The detail message is saved for later retrieval by the {@link #getMessage()} method.
     * </p>
     *
     * @param message The detail message explaining the reason for the exception
     * (e.g., "Recipe not found with ID: 123").
     */
    public RecipeNotFoundException(String message) {
        super(message);
    }
}