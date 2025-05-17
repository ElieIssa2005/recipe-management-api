package com.example.recipeoop_1.service;

import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.exception.RecipeNotFoundException;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RecipeServiceImpl}.
 * These tests focus on the business logic within the service layer,
 * mocking dependencies like MongoTemplate and CategoryService.
 * Uses a more forgiving 'any(Query.class)' for matching query arguments.
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private Recipe testRecipe1;
    private Recipe testRecipe2;

    @BeforeEach
    void setUp() {
        // Initialize common test objects
        testRecipe1 = new Recipe();
        testRecipe1.setId("recipe1_id");
        testRecipe1.setTitle("Pasta Carbonara");
        testRecipe1.setIngredients(Arrays.asList("Spaghetti", "Eggs", "Pancetta", "Pecorino Romano", "Black Pepper"));
        testRecipe1.setInstructions("Cook spaghetti. Fry pancetta. Mix eggs and cheese. Combine all.");
        testRecipe1.setCookingTime(20);
        testRecipe1.setCategory("Main Course");
        testRecipe1.setCreatedBy("user1");

        testRecipe2 = new Recipe();
        testRecipe2.setId("recipe2_id");
        testRecipe2.setTitle("Chocolate Cake");
        testRecipe2.setIngredients(Arrays.asList("Flour", "Sugar", "Cocoa Powder", "Eggs", "Milk"));
        testRecipe2.setInstructions("Mix dry ingredients. Mix wet ingredients. Combine. Bake.");
        testRecipe2.setCookingTime(60);
        testRecipe2.setCategory("Dessert");
        testRecipe2.setCreatedBy("user2");
    }

    /**
     * Test for creating a new recipe successfully.
     */
    @Test
    void createRecipe_shouldCreateAndReturnRecipe() {
        // Arrange
        Recipe newRecipe = new Recipe();
        newRecipe.setTitle("New Test Recipe");
        newRecipe.setCategory("TestCategory");

        String username = "testUser";
        String formattedCollectionName = "recipe_testcategory";

        doNothing().when(categoryService).ensureCategoryExists(eq("TestCategory"));
        when(mongoTemplate.insert(any(Recipe.class), eq(formattedCollectionName))).thenReturn(newRecipe);

        // Act
        Recipe createdRecipe = recipeService.createRecipe(newRecipe, username);

        // Assert
        assertNotNull(createdRecipe);
        assertEquals(username, createdRecipe.getCreatedBy());
        assertEquals("TestCategory", createdRecipe.getCategory());
        verify(categoryService).ensureCategoryExists(eq("TestCategory"));
        verify(mongoTemplate).insert(eq(newRecipe), eq(formattedCollectionName));
    }

    /**
     * Test for creating a recipe with a null or empty category.
     */
    @Test
    void createRecipe_withNullOrEmptyCategory_shouldDefaultToUncategorized() {
        // Arrange
        Recipe newRecipe = new Recipe();
        newRecipe.setTitle("Uncategorized Recipe");
        newRecipe.setCategory(null);
        String username = "testUser";
        String uncategorizedCollectionName = "recipe_uncategorized";

        doNothing().when(categoryService).ensureCategoryExists(eq("uncategorized"));
        when(mongoTemplate.insert(any(Recipe.class), eq(uncategorizedCollectionName))).thenAnswer(invocation -> {
            Recipe recipeToInsert = invocation.getArgument(0);
            assertEquals("uncategorized", recipeToInsert.getCategory());
            return recipeToInsert;
        });

        // Act
        Recipe createdRecipe = recipeService.createRecipe(newRecipe, username);

        // Assert
        assertNotNull(createdRecipe);
        assertEquals("uncategorized", createdRecipe.getCategory());
        verify(categoryService).ensureCategoryExists(eq("uncategorized"));
        verify(mongoTemplate).insert(any(Recipe.class), eq(uncategorizedCollectionName));

        newRecipe.setCategory("  ");
        recipeService.createRecipe(newRecipe, username);
        verify(categoryService, times(2)).ensureCategoryExists(eq("uncategorized"));
    }


    /**
     * Test for retrieving all recipes from all categories.
     */
    @Test
    void getAllRecipes_shouldReturnAllRecipesFromAllCategories() {
        // Arrange
        List<String> categories = Arrays.asList("Main Course", "Dessert");
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.collectionExists(mainCourseCollection)).thenReturn(true);
        when(mongoTemplate.collectionExists(dessertCollection)).thenReturn(true);
        when(mongoTemplate.findAll(Recipe.class, mainCourseCollection)).thenReturn(Collections.singletonList(testRecipe1));
        when(mongoTemplate.findAll(Recipe.class, dessertCollection)).thenReturn(Collections.singletonList(testRecipe2));

        // Act
        List<Recipe> allRecipes = recipeService.getAllRecipes();

        // Assert
        assertNotNull(allRecipes);
        assertEquals(2, allRecipes.size());
        assertTrue(allRecipes.contains(testRecipe1));
        assertTrue(allRecipes.contains(testRecipe2));
        verify(categoryService).getAllCategories();
        verify(mongoTemplate).findAll(Recipe.class, mainCourseCollection);
        verify(mongoTemplate).findAll(Recipe.class, dessertCollection);
    }

    /**
     * Test retrieving a specific recipe by its ID and category when it exists.
     */
    @Test
    void getRecipeById_withCategory_whenExists_shouldReturnRecipe() {
        // Arrange
        String category = "Main Course";
        String recipeId = "recipe1_id";
        String collectionName = "recipe_main_course";
        // Using forgiving any(Query.class)
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(collectionName))).thenReturn(testRecipe1);

        // Act
        Recipe foundRecipe = recipeService.getRecipeById(category, recipeId);

        // Assert
        assertNotNull(foundRecipe);
        assertEquals(recipeId, foundRecipe.getId());
        assertEquals(category, foundRecipe.getCategory());
        verify(mongoTemplate).findOne(any(Query.class), eq(Recipe.class), eq(collectionName));
    }

    /**
     * Test retrieving a specific recipe by its ID and category when it does not exist.
     */
    @Test
    void getRecipeById_withCategory_whenNotExists_shouldReturnNull() {
        // Arrange
        String category = "NonExistentCategory";
        String recipeId = "non_existent_id";
        String collectionName = "recipe_nonexistentcategory";
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(collectionName))).thenReturn(null);

        // Act
        Recipe foundRecipe = recipeService.getRecipeById(category, recipeId);

        // Assert
        assertNull(foundRecipe);
        verify(mongoTemplate).findOne(any(Query.class), eq(Recipe.class), eq(collectionName));
    }

    /**
     * Test retrieving a recipe by ID (searching across all categories) when it exists.
     */
    @Test
    void getRecipeById_globalSearch_whenExists_shouldReturnRecipe() {
        // Arrange
        String recipeId = "recipe2_id";
        List<String> categories = Arrays.asList("Main Course", "Dessert");
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(categories);
        // Using forgiving any(Query.class)
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(mainCourseCollection))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(dessertCollection))).thenReturn(testRecipe2);

        // Act
        Recipe foundRecipe = recipeService.getRecipeById(recipeId);

        // Assert
        assertNotNull(foundRecipe);
        assertEquals(recipeId, foundRecipe.getId());
        assertEquals("Dessert", foundRecipe.getCategory());
        verify(categoryService).getAllCategories();
        verify(mongoTemplate).findOne(any(Query.class), eq(Recipe.class), eq(mainCourseCollection));
        verify(mongoTemplate).findOne(any(Query.class), eq(Recipe.class), eq(dessertCollection));
    }

    /**
     * Test retrieving a recipe by ID (searching across all categories) when it does not exist.
     */
    @Test
    void getRecipeById_globalSearch_whenNotExists_shouldThrowRecipeNotFoundException() {
        // Arrange
        String recipeId = "non_existent_id";
        List<String> categories = Arrays.asList("Main Course", "Dessert");
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(mainCourseCollection))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(dessertCollection))).thenReturn(null);

        // Act & Assert
        assertThrows(RecipeNotFoundException.class, () -> recipeService.getRecipeById(recipeId));
        verify(categoryService).getAllCategories();
        verify(mongoTemplate, times(2)).findOne(any(Query.class), eq(Recipe.class), anyString());
    }

    /**
     * Test updating a recipe when its category does not change.
     */
    @Test
    void updateRecipe_categoryUnchanged_shouldUpdateInPlace() {
        // Arrange
        String recipeId = testRecipe1.getId();
        Recipe recipeDetailsToUpdate = new Recipe();
        recipeDetailsToUpdate.setTitle("Updated Pasta Carbonara");
        recipeDetailsToUpdate.setCategory("Main Course");

        String collectionName = "recipe_main_course";

        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList("Main Course"));
        // Using forgiving any(Query.class) for findOne
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(collectionName))).thenReturn(testRecipe1);
        when(mongoTemplate.save(any(Recipe.class), eq(collectionName))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Recipe updatedRecipe = recipeService.updateRecipe(recipeId, recipeDetailsToUpdate);

        // Assert
        assertNotNull(updatedRecipe);
        assertEquals(recipeId, updatedRecipe.getId());
        assertEquals("Updated Pasta Carbonara", updatedRecipe.getTitle());
        assertEquals("Main Course", updatedRecipe.getCategory());
        assertEquals(testRecipe1.getCreatedBy(), updatedRecipe.getCreatedBy());
        verify(mongoTemplate).save(any(Recipe.class), eq(collectionName));
        verify(mongoTemplate, never()).remove(any(Query.class), eq(Recipe.class), anyString());
        verify(mongoTemplate, never()).insert(any(Recipe.class), anyString());
        verify(mongoTemplate, never()).dropCollection(anyString());
    }

    /**
     * Test updating a recipe when its category changes.
     */
    @Test
    void updateRecipe_categoryChanged_shouldMoveRecipeAndDropOldCollectionIfEmpty() {
        // Arrange
        String recipeId = testRecipe1.getId();
        Recipe recipeDetailsToUpdate = new Recipe();
        recipeDetailsToUpdate.setTitle("Pasta moved to Appetizers");
        recipeDetailsToUpdate.setCategory("Appetizer");

        String oldCollectionName = "recipe_main_course";
        String newCollectionName = "recipe_appetizer";

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList("Main Course", "Dessert"));
        // Using forgiving any(Query.class) for findOne
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(oldCollectionName))).thenReturn(testRecipe1);
        // Using forgiving any(Query.class) for remove
        when(mongoTemplate.remove(any(Query.class), eq(Recipe.class), eq(oldCollectionName))).thenReturn(null);
        when(mongoTemplate.collectionExists(oldCollectionName)).thenReturn(true);
        when(mongoTemplate.count(any(Query.class), eq(oldCollectionName))).thenReturn(0L);
        doNothing().when(mongoTemplate).dropCollection(oldCollectionName);
        doNothing().when(categoryService).ensureCategoryExists(eq("Appetizer"));
        when(mongoTemplate.insert(any(Recipe.class), eq(newCollectionName))).thenAnswer(invocation -> {
            Recipe inserted = invocation.getArgument(0);
            assertEquals(recipeId, inserted.getId());
            assertEquals(testRecipe1.getCreatedBy(), inserted.getCreatedBy());
            return inserted;
        });

        // Act
        Recipe updatedRecipe = recipeService.updateRecipe(recipeId, recipeDetailsToUpdate);

        // Assert
        assertNotNull(updatedRecipe);
        assertEquals(recipeId, updatedRecipe.getId());
        assertEquals("Pasta moved to Appetizers", updatedRecipe.getTitle());
        assertEquals("Appetizer", updatedRecipe.getCategory());
        verify(mongoTemplate).remove(any(Query.class), eq(Recipe.class), eq(oldCollectionName));
        verify(mongoTemplate).collectionExists(oldCollectionName);
        verify(mongoTemplate).count(any(Query.class), eq(oldCollectionName));
        verify(mongoTemplate).dropCollection(oldCollectionName);
        verify(categoryService).ensureCategoryExists(eq("Appetizer"));
        verify(mongoTemplate).insert(any(Recipe.class), eq(newCollectionName));
        verify(mongoTemplate, never()).save(any(Recipe.class), anyString());
    }

    /**
     * Test updating a recipe when category changes, but old collection is "uncategorized".
     */
    @Test
    void updateRecipe_categoryChanged_oldIsUncategorized_shouldNotDropUncategorized() {
        // Arrange
        testRecipe1.setCategory("uncategorized");
        String recipeId = testRecipe1.getId();
        Recipe recipeDetailsToUpdate = new Recipe();
        recipeDetailsToUpdate.setTitle("Moved from Uncategorized");
        recipeDetailsToUpdate.setCategory("NewCategory");

        String oldCollectionName = "recipe_uncategorized";
        String newCollectionName = "recipe_newcategory";

        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList("uncategorized"));
        // Using forgiving any(Query.class) for findOne
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(oldCollectionName))).thenReturn(testRecipe1);
        // Using forgiving any(Query.class) for remove
        when(mongoTemplate.remove(any(Query.class), eq(Recipe.class), eq(oldCollectionName))).thenReturn(null);
        doNothing().when(categoryService).ensureCategoryExists(eq("NewCategory"));
        when(mongoTemplate.insert(any(Recipe.class), eq(newCollectionName))).thenReturn(recipeDetailsToUpdate);

        // Act
        recipeService.updateRecipe(recipeId, recipeDetailsToUpdate);

        // Assert
        verify(mongoTemplate).remove(any(Query.class), eq(Recipe.class), eq(oldCollectionName));
        verify(mongoTemplate, never()).dropCollection(oldCollectionName);
        verify(categoryService).ensureCategoryExists(eq("NewCategory"));
        verify(mongoTemplate).insert(any(Recipe.class), eq(newCollectionName));
    }


    /**
     * Test deleting a recipe successfully.
     */
    @Test
    void deleteRecipe_shouldDeleteRecipeAndDropCollectionIfEmpty() {
        // Arrange
        String recipeId = testRecipe1.getId();
        String collectionName = "recipe_main_course";

        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList("Main Course"));
        // Using forgiving any(Query.class) for findOne
        when(mongoTemplate.findOne(any(Query.class), eq(Recipe.class), eq(collectionName))).thenReturn(testRecipe1);
        // Using forgiving any(Query.class) for remove
        when(mongoTemplate.remove(any(Query.class), eq(Recipe.class), eq(collectionName))).thenReturn(null);
        when(mongoTemplate.collectionExists(collectionName)).thenReturn(true);
        when(mongoTemplate.count(any(Query.class), eq(collectionName))).thenReturn(0L);
        doNothing().when(mongoTemplate).dropCollection(collectionName);

        // Act
        recipeService.deleteRecipe(recipeId);

        // Assert
        verify(mongoTemplate).remove(any(Query.class), eq(Recipe.class), eq(collectionName));
        verify(mongoTemplate).collectionExists(collectionName);
        verify(mongoTemplate).count(any(Query.class), eq(collectionName));
        verify(mongoTemplate).dropCollection(collectionName);
    }

    /**
     * Test deleting a recipe when the recipe is not found.
     */
    @Test
    void deleteRecipe_whenNotExists_shouldThrowRecipeNotFoundException() {
        // Arrange
        String recipeId = "non_existent_id";
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RecipeNotFoundException.class, () -> recipeService.deleteRecipe(recipeId));
        verify(mongoTemplate, never()).remove(any(Query.class), any(Class.class), anyString());
        verify(mongoTemplate, never()).dropCollection(anyString());
    }

    /**
     * Test retrieving recipes by a specific user.
     */
    @Test
    void getRecipesByUser_shouldReturnUserSpecificRecipes() {
        // Arrange
        String username = "user1";
        List<String> categories = Arrays.asList("Main Course", "Appetizer");
        String mainCourseCollection = "recipe_main_course";
        String appetizerCollection = "recipe_appetizer";

        Recipe user1Appetizer = new Recipe();
        user1Appetizer.setId("appetizer1");
        user1Appetizer.setTitle("Bruschetta");
        user1Appetizer.setCategory("Appetizer");
        user1Appetizer.setCreatedBy(username);

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.collectionExists(mainCourseCollection)).thenReturn(true);
        when(mongoTemplate.collectionExists(appetizerCollection)).thenReturn(true);

        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection)))
                .thenReturn(Collections.singletonList(testRecipe1));
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(appetizerCollection)))
                .thenReturn(Collections.singletonList(user1Appetizer));

        // Act
        List<Recipe> userRecipes = recipeService.getRecipesByUser(username);

        // Assert
        assertNotNull(userRecipes);
        assertEquals(2, userRecipes.size());
        assertTrue(userRecipes.stream().allMatch(r -> r.getCreatedBy().equals(username)));
        assertTrue(userRecipes.contains(testRecipe1));
        assertTrue(userRecipes.contains(user1Appetizer));
        verify(categoryService).getAllCategories();
        // Verify find was called for each collection, but not the specifics of the query
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection));
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(appetizerCollection));
    }

    /**
     * Test searching recipes by title.
     */
    @Test
    void searchRecipesByTitle_shouldReturnMatchingRecipes() {
        // Arrange
        String titleKeyword = "Pasta";
        List<String> categories = Collections.singletonList("Main Course");
        String collectionName = "recipe_main_course";

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.collectionExists(collectionName)).thenReturn(true);
        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(collectionName)))
                .thenReturn(Collections.singletonList(testRecipe1));

        // Act
        List<Recipe> foundRecipes = recipeService.searchRecipesByTitle(titleKeyword);

        // Assert
        assertNotNull(foundRecipes);
        assertEquals(1, foundRecipes.size());
        assertEquals(testRecipe1.getTitle(), foundRecipes.get(0).getTitle()); // Still good to check returned data
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(collectionName));
    }

    /**
     * Test searching recipes by a specific category.
     */
    @Test
    void searchRecipesByCategory_shouldReturnRecipesFromThatCategory() {
        // Arrange
        String categoryToSearch = "Dessert";
        String collectionName = "recipe_dessert";

        when(mongoTemplate.collectionExists(collectionName)).thenReturn(true);
        when(mongoTemplate.findAll(Recipe.class, collectionName)).thenReturn(Collections.singletonList(testRecipe2));

        // Act
        List<Recipe> foundRecipes = recipeService.searchRecipesByCategory(categoryToSearch);

        // Assert
        assertNotNull(foundRecipes);
        assertEquals(1, foundRecipes.size());
        assertEquals(testRecipe2.getTitle(), foundRecipes.get(0).getTitle());
        assertEquals(categoryToSearch, foundRecipes.get(0).getCategory());
        verify(mongoTemplate).findAll(Recipe.class, collectionName);
    }

    /**
     * Test searching recipes by category when the category (collection) does not exist.
     */
    @Test
    void searchRecipesByCategory_whenCategoryNotExists_shouldReturnEmptyList() {
        // Arrange
        String categoryToSearch = "NonExistentCategory";
        String collectionName = "recipe_nonexistentcategory";
        when(mongoTemplate.collectionExists(collectionName)).thenReturn(false);

        // Act
        List<Recipe> foundRecipes = recipeService.searchRecipesByCategory(categoryToSearch);

        // Assert
        assertNotNull(foundRecipes);
        assertTrue(foundRecipes.isEmpty());
        verify(mongoTemplate, never()).findAll(Recipe.class, collectionName);
    }


    /**
     * Test searching recipes by cooking time.
     */
    @Test
    void searchRecipesByCookingTime_shouldReturnMatchingRecipes() {
        // Arrange
        Integer maxCookingTime = 30;
        List<String> categories = Arrays.asList("Main Course", "Dessert");
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.collectionExists(mainCourseCollection)).thenReturn(true);
        when(mongoTemplate.collectionExists(dessertCollection)).thenReturn(true);

        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection)))
                .thenReturn(Collections.singletonList(testRecipe1));
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(dessertCollection)))
                .thenReturn(Collections.emptyList());

        // Act
        List<Recipe> foundRecipes = recipeService.searchRecipesByCookingTime(maxCookingTime);

        // Assert
        assertNotNull(foundRecipes);
        assertEquals(1, foundRecipes.size());
        assertEquals(testRecipe1.getTitle(), foundRecipes.get(0).getTitle());
        assertTrue(foundRecipes.get(0).getCookingTime() <= maxCookingTime);
        verify(mongoTemplate, times(2)).find(any(Query.class), eq(Recipe.class), anyString());
    }

    /**
     * Test searching recipes by ingredient.
     */
    @Test
    void searchRecipesByIngredient_shouldReturnMatchingRecipes() {
        // Arrange
        String ingredientKeyword = "Eggs";
        List<String> categories = Arrays.asList("Main Course", "Dessert");
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(categories);
        when(mongoTemplate.collectionExists(mainCourseCollection)).thenReturn(true);
        when(mongoTemplate.collectionExists(dessertCollection)).thenReturn(true);

        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection)))
                .thenReturn(Collections.singletonList(testRecipe1));
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(dessertCollection)))
                .thenReturn(Collections.singletonList(testRecipe2));

        // Act
        List<Recipe> foundRecipes = recipeService.searchRecipesByIngredient(ingredientKeyword);

        // Assert
        assertNotNull(foundRecipes);
        assertEquals(2, foundRecipes.size());
        assertTrue(foundRecipes.contains(testRecipe1));
        assertTrue(foundRecipes.contains(testRecipe2));
        verify(mongoTemplate, times(2)).find(any(Query.class), eq(Recipe.class), anyString());
    }

    /**
     * Test advanced search with a specific category.
     */
    @Test
    void advancedSearch_withSpecificCategory_shouldSearchInThatCategory() {
        // Arrange
        String title = "Cake";
        String category = "Dessert";
        Integer maxCookingTime = 70;
        String ingredient = "Flour";
        String dessertCollection = "recipe_dessert";

        when(mongoTemplate.collectionExists(dessertCollection)).thenReturn(true);
        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(dessertCollection)))
                .thenReturn(Collections.singletonList(testRecipe2));

        // Act
        List<Recipe> results = recipeService.advancedSearch(title, category, maxCookingTime, ingredient);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testRecipe2.getTitle(), results.get(0).getTitle());
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(dessertCollection));
        verify(categoryService, never()).getAllCategories();
    }

    /**
     * Test advanced search without a specific category (searches all categories).
     */
    @Test
    void advancedSearch_withoutSpecificCategory_shouldSearchAllCategories() {
        // Arrange
        String title = "Pasta";
        Integer maxCookingTime = 25;
        String mainCourseCollection = "recipe_main_course";
        String dessertCollection = "recipe_dessert";

        when(categoryService.getAllCategories()).thenReturn(Arrays.asList("Main Course", "Dessert"));
        when(mongoTemplate.collectionExists(mainCourseCollection)).thenReturn(true);
        when(mongoTemplate.collectionExists(dessertCollection)).thenReturn(true);

        // Using forgiving any(Query.class)
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection)))
                .thenReturn(Collections.singletonList(testRecipe1));
        when(mongoTemplate.find(any(Query.class), eq(Recipe.class), eq(dessertCollection)))
                .thenReturn(Collections.emptyList());

        // Act
        List<Recipe> results = recipeService.advancedSearch(title, null, maxCookingTime, null);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testRecipe1.getTitle(), results.get(0).getTitle());
        verify(categoryService).getAllCategories();
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(mainCourseCollection));
        verify(mongoTemplate).find(any(Query.class), eq(Recipe.class), eq(dessertCollection));
    }

    /**
     * Test advanced search when the specified category does not exist.
     */
    @Test
    void advancedSearch_withNonExistentCategory_shouldReturnEmptyList() {
        // Arrange
        String category = "NonExistent";
        String collectionName = "recipe_nonexistent";
        when(mongoTemplate.collectionExists(collectionName)).thenReturn(false);

        // Act
        List<Recipe> results = recipeService.advancedSearch(null, category, null, null);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), any(Class.class), anyString());
    }
}