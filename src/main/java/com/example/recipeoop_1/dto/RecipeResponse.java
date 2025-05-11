package com.example.recipeoop_1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import com.example.recipeoop_1.model.Recipe;

import java.util.List;

public class RecipeResponse {

    @Schema(description = "List of recipes")
    private List<Recipe> content;

    @Schema(description = "Current page number")
    private int pageNo;

    @Schema(description = "Page size")
    private int pageSize;

    @Schema(description = "Total elements")
    private long totalElements;

    @Schema(description = "Total pages")
    private int totalPages;

    @Schema(description = "Is last page")
    private boolean last;

    public RecipeResponse() {
    }

    public RecipeResponse(List<Recipe> content, int pageNo, int pageSize, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public static RecipeResponse fromPageable(Page<Recipe> recipePage) {
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
    public List<Recipe> getContent() {
        return content;
    }

    public void setContent(List<Recipe> content) {
        this.content = content;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}