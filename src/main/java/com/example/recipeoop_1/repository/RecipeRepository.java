package com.example.recipeoop_1.repository;

import com.example.recipeoop_1.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {

    // Find recipes by createdBy field with pagination
    Page<Recipe> findByCreatedBy(String username, Pageable pageable);

    // Find recipes by title (case-insensitive)
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Recipe> findByTitleContainingIgnoreCase(String title);

    // Find recipes by category (case-insensitive)
    @Query("{'category': {$regex: ?0, $options: 'i'}}")
    List<Recipe> findByCategoryContainingIgnoreCase(String category);

    // Find recipes by cooking time less than or equal to specified minutes
    List<Recipe> findByCookingTimeLessThanEqual(Integer cookingTime);
}