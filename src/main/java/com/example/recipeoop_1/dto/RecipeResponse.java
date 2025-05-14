package com.example.recipeoop_1.dto;

// import io.swagger.v3.oas.annotations.media.Schema; // Ensure this is removed if present
import org.springframework.data.domain.Page;
import com.example.recipeoop_1.model.Recipe; // Assuming Recipe model is in this package

import java.util.List;

/**
 * Data Transfer Object (DTO) for representing a paginated list of recipes.
 * <p>
 * This class is typically used as a standardized response format for API endpoints
 * that return recipes with pagination details such as page number, page size,
 * total elements, and total pages.
 * </p>
 *
 * @author Your Name/Team Name
 * @version 1.0
 * @since 2025-05-14
 * @see com.example.recipeoop_1.model.Recipe
 * @see org.springframework.data.domain.Page
 */
public class RecipeResponse {

    /**
     * The list of {@link Recipe} objects for the current page.
     */
    // @Schema(description = "List of recipes") // Removed
    private List<Recipe> content;

    /**
     * The current page number (0-indexed).
     */
    // @Schema(description = "Current page number") // Removed
    private int pageNo;

    /**
     * The number of recipes requested per page.
     */
    // @Schema(description = "Page size") // Removed
    private int pageSize;

    /**
     * The total number of recipes available across all pages.
     */
    // @Schema(description = "Total elements") // Removed
    private long totalElements;

    /**
     * The total number of pages available.
     */
    // @Schema(description = "Total pages") // Removed
    private int totalPages;

    /**
     * A boolean flag indicating if the current page is the last page.
     * {@code true} if this is the last page, {@code false} otherwise.
     */
    // @Schema(description = "Is last page") // Removed
    private boolean last;

    /**
     * Default constructor for {@link RecipeResponse}.
     * Required for frameworks like Jackson for JSON deserialization, although this class
     * is primarily used for serialization (API responses).
     */
    public RecipeResponse() {
    }

    /**
     * Constructs a {@link RecipeResponse} with all pagination details and content.
     *
     * @param content The list of {@link Recipe} objects for the current page.
     * @param pageNo The current page number.
     * @param pageSize The size of the page.
     * @param totalElements The total number of recipes available.
     * @param totalPages The total number of pages.
     * @param last A boolean indicating if this is the last page.
     */
    public RecipeResponse(List<Recipe> content, int pageNo, int pageSize, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    /**
     * Static factory method to create a {@link RecipeResponse} from a Spring Data {@link Page} object.
     * <p>
     * This provides a convenient way to convert the result of a paginated repository query
     * into the standardized {@link RecipeResponse} format.
     * </p>
     *
     * @param recipePage The {@link Page} object containing {@link Recipe} instances and pagination information.
     * Must not be {@code null}.
     * @return A new {@link RecipeResponse} populated with data from the provided {@code recipePage}.
     */
    public static RecipeResponse fromPageable(Page<Recipe> recipePage) {
        if (recipePage == null) {
            // Or throw IllegalArgumentException, or return an empty RecipeResponse
            // Depending on desired behavior for null input.
            // For now, let's assume recipePage is never null based on typical usage.
            return new RecipeResponse(List.of(), 0, 0, 0, 0, true);
        }
        return new RecipeResponse(
                recipePage.getContent(),
                recipePage.getNumber(),
                recipePage.getSize(),
                recipePage.getTotalElements(),
                recipePage.getTotalPages(),
                recipePage.isLast()
        );
    }

    // Getters and Setters

    /**
     * Gets the list of recipes for the current page.
     *
     * @return A list of {@link Recipe} objects.
     */
    public List<Recipe> getContent() {
        return content;
    }

    /**
     * Sets the list of recipes for the current page.
     *
     * @param content A list of {@link Recipe} objects to set.
     */
    public void setContent(List<Recipe> content) {
        this.content = content;
    }

    /**
     * Gets the current page number (0-indexed).
     *
     * @return The current page number.
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * Sets the current page number.
     *
     * @param pageNo The page number to set (0-indexed).
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * Gets the number of recipes requested per page.
     *
     * @return The page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the number of recipes per page.
     *
     * @param pageSize The page size to set.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets the total number of recipes available across all pages.
     *
     * @return The total number of elements.
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Sets the total number of recipes available.
     *
     * @param totalElements The total number of elements to set.
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Gets the total number of pages available.
     *
     * @return The total number of pages.
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total number of pages.
     *
     * @param totalPages The total number of pages to set.
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Checks if the current page is the last page.
     *
     * @return {@code true} if this is the last page, {@code false} otherwise.
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Sets whether the current page is the last page.
     *
     * @param last {@code true} if this is the last page, {@code false} otherwise.
     */
    public void setLast(boolean last) {
        this.last = last;
    }
}