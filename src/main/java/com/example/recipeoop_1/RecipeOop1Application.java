package com.example.recipeoop_1;

//deploy test comment, can be removed if not needed for deployment tracking
import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Main application class for the Recipe Management System (RecipeOOP_1).
 * <p>
 * This class serves as the entry point for the Spring Boot application.
 * It is annotated with {@link SpringBootApplication}, which enables auto-configuration,
 * component scanning, and other Spring Boot features.
 * {@link EnableMongoRepositories} is used to enable Spring Data MongoDB repositories.
 * </p>
 * <p>
 * The application includes a {@link ConsoleUI} component that provides a command-line
 * interface for interacting with the recipe management system. This console UI is
 * intended for local testing and development and is only activated when the application
 * is not running with the "prod" (production) Spring profile active.
 * </p>
 *
 * @author Your Name/Team Name (Original authors: Elie Issa - Michel Ghazaly, as per project context)
 * @version 1.1
 * @since 2025-05-14
 */
@SpringBootApplication
@EnableMongoRepositories // Enables Spring Data MongoDB repositories
public class RecipeOop1Application {

    /**
     * The main method that serves as the entry point for the Spring Boot application.
     * <p>
     * It launches the Spring application context. If the application is not running
     * under the "prod" profile, it retrieves the {@link ConsoleUI} bean and starts
     * the console-based user interface. In a "prod" environment, the console UI
     * is skipped, and the application context remains open to serve web requests.
     * </p>
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RecipeOop1Application.class, args);

        // Check if running in production profile
        Environment env = context.getEnvironment();
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        // Only run console UI if not in production mode
        if (!isProd) {
            try {
                ConsoleUI consoleUI = context.getBean(ConsoleUI.class);
                consoleUI.start(); // Start the interactive console UI
            } catch (Exception e) {
                // Log or handle exception if ConsoleUI cannot be started
                System.err.println("Failed to start ConsoleUI: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Close the context only if not in production and after ConsoleUI finishes or fails.
                // In a typical web application, the context is not closed here to keep the server running.
                // For a CLI app that should exit, closing the context is appropriate.
                if (!isProd) { // Double-check, though already in this block
                    context.close(); // Ensure resources are released after CLI finishes
                }
            }
        }
        // If 'isProd' is true, the application continues to run as a web server.
    }
}

/**
 * A Spring {@link Component} that implements {@link CommandLineRunner} to provide
 * a console-based user interface for the Recipe Management System.
 * <p>
 * This UI is primarily intended for local development, testing, and demonstration purposes.
 * It allows users to interact with the recipe service through a series of text-based menus
 * for operations like viewing, adding, editing, deleting, and searching recipes.
 * The {@link ConsoleUI#start()} method is manually called from the main application
 * class when not in a "prod" environment.
 * </p>
 *
 * @see CommandLineRunner
 * @see RecipeService
 * @see CategoryService
 */
@Component
class ConsoleUI implements CommandLineRunner {
    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final Scanner scanner;

    /**
     * The password required for accessing admin functionalities in the console UI.
     * Hardcoded for simplicity in this demonstration UI.
     */
    private static final String ADMIN_PASSWORD = "1234";

    /**
     * Constructs a {@code ConsoleUI} with necessary service dependencies.
     *
     * @param recipeService The {@link RecipeService} for recipe-related operations.
     * @param categoryService The {@link CategoryService} for category-related operations.
     */
    public ConsoleUI(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
        this.scanner = new Scanner(System.in); // Initialize scanner for user input
    }

    /**
     * Callback method from {@link CommandLineRunner}.
     * <p>
     * In this application, this method is intentionally left empty because the console UI
     * is started explicitly via the {@link #start()} method from the main application logic
     * when not in a "prod" profile. This provides more control over when the UI is initiated.
     * </p>
     *
     * @param args Incoming command-line arguments, not used by this UI.
     */
    @Override
    public void run(String... args) {
        // This method is called automatically by Spring Boot if ConsoleUI were the primary runner.
        // However, we manually call start() from the main application method
        // when not in "prod" profile for more control.
    }

    /**
     * Starts the main loop of the console-based user interface.
     * <p>
     * Presents the main menu to the user, allowing them to choose between logging in
     * as a client, logging in as an admin, or exiting the application.
     * The loop continues until the user chooses to exit.
     * </p>
     */
    public void start() {
        System.out.println("Welcome to Recipe Management System! (Console UI)");

        boolean exit = false;
        while (!exit) {
            System.out.println("\nPlease select an option:");
            System.out.println("1. Login as Client");
            System.out.println("2. Login as Admin");
            System.out.println("3. Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    clientMenu();
                    break;
                case "2":
                    adminAuthentication();
                    break;
                case "3":
                    exit = true;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close(); // Close the scanner when the UI exits
    }

    /**
     * Displays the client menu, allowing users to browse recipes by category or search.
     * <p>
     * Lists available categories and provides options to view recipes within a category,
     * initiate a search, or return to the main menu.
     * </p>
     */
    private void clientMenu() {
        boolean backToMain = false;
        while (!backToMain) {
            List<String> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("No recipe categories found in the database.");
                // Optionally offer to search or return
                System.out.println("1. Search for recipes");
                System.out.println("2. Back to main menu");
                System.out.print("Your choice: ");
                String choice = scanner.nextLine();
                if ("1".equals(choice)) searchRecipesMenu();
                return; // or backToMain = true; if "2"
            }

            System.out.println("\nAvailable Categories:");
            for (int i = 0; i < categories.size(); i++) {
                // Assuming category names from service might still have "recipe_" or be formatted.
                // The replace call here might be redundant if categoryService.getAllCategories()
                // already returns clean names.
                System.out.println((i + 1) + ". " + categories.get(i).replace("recipe_", ""));
            }
            System.out.println((categories.size() + 1) + ". Search for recipes");
            System.out.println((categories.size() + 2) + ". Back to main menu");
            System.out.print("Select a category or option: ");
            String choice = scanner.nextLine();

            try {
                int categoryIndex = Integer.parseInt(choice) - 1;
                if (categoryIndex >= 0 && categoryIndex < categories.size()) {
                    String selectedCategory = categories.get(categoryIndex).replace("recipe_", "");
                    showRecipesByCategory(selectedCategory);
                } else if (categoryIndex == categories.size()) {
                    searchRecipesMenu();
                } else if (categoryIndex == categories.size() + 1) {
                    backToMain = true;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Displays the search menu, offering various ways to find recipes.
     * <p>
     * Options include searching by title, cooking time, ingredient, or performing
     * an advanced search with multiple criteria.
     * </p>
     */
    private void searchRecipesMenu() {
        boolean backToClientMenu = false;
        while (!backToClientMenu) {
            System.out.println("\nSearch Options:");
            System.out.println("1. Search by title");
            System.out.println("2. Search by cooking time (max minutes)");
            System.out.println("3. Search by ingredient");
            System.out.println("4. Advanced search (multiple criteria)");
            System.out.println("5. Back to category menu");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();
            List<Recipe> searchResults = new ArrayList<>();
            boolean performDisplay = true; // Flag to control if displayRecipeList is called

            switch (choice) {
                case "1":
                    System.out.print("Enter title keyword: ");
                    String title = scanner.nextLine();
                    searchResults = recipeService.searchRecipesByTitle(title);
                    break;
                case "2":
                    System.out.print("Enter maximum cooking time (minutes): ");
                    try {
                        int cookingTime = Integer.parseInt(scanner.nextLine());
                        searchResults = recipeService.searchRecipesByCookingTime(cookingTime);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input for cooking time. Please enter a number.");
                        performDisplay = false; // Skip display as search didn't run correctly
                    }
                    break;
                case "3":
                    System.out.print("Enter ingredient keyword: ");
                    String ingredient = scanner.nextLine();
                    searchResults = recipeService.searchRecipesByIngredient(ingredient);
                    break;
                case "4":
                    advancedSearchMenu(); // This method handles its own display
                    performDisplay = false; // Skip display in this loop
                    break;
                case "5":
                    backToClientMenu = true;
                    performDisplay = false; // Don't display, just go back
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    performDisplay = false; // Skip display for invalid choice
            }

            if (performDisplay) {
                if (searchResults.isEmpty()) {
                    System.out.println("No recipes found matching your search criteria.");
                } else {
                    displayRecipeList("Search Results", searchResults);
                }
            }
        }
    }

    /**
     * Handles the advanced search functionality, prompting the user for multiple optional criteria.
     * <p>
     * Allows searching by title, category, maximum cooking time, and ingredient.
     * Empty inputs for criteria are treated as "not specified".
     * </p>
     */
    private void advancedSearchMenu() {
        System.out.println("\nAdvanced Search (leave blank or press Enter to skip a criterion):");

        System.out.print("Title contains: ");
        String title = scanner.nextLine().trim();
        title = title.isEmpty() ? null : title;

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        category = category.isEmpty() ? null : category;

        System.out.print("Maximum cooking time (minutes): ");
        String cookingTimeStr = scanner.nextLine().trim();
        Integer cookingTime = null;
        if (!cookingTimeStr.isEmpty()) {
            try {
                cookingTime = Integer.parseInt(cookingTimeStr);
                if (cookingTime < 0) {
                    System.out.println("Cooking time cannot be negative. Ignoring this criterion.");
                    cookingTime = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid cooking time format. Ignoring this criterion.");
            }
        }

        System.out.print("Ingredient contains: ");
        String ingredient = scanner.nextLine().trim();
        ingredient = ingredient.isEmpty() ? null : ingredient;

        List<Recipe> searchResults = recipeService.advancedSearch(title, category, cookingTime, ingredient);
        if (searchResults.isEmpty()) {
            System.out.println("No recipes found matching your advanced search criteria.");
        } else {
            displayRecipeList("Advanced Search Results", searchResults);
        }
    }

    /**
     * Displays a list of recipes to the console and allows the user to select one to view its details.
     *
     * @param listTitle A title for the displayed list (e.g., "Search Results", "Recipes in Category X").
     * @param recipes The list of {@link Recipe} objects to display.
     */
    private void displayRecipeList(String listTitle, List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            System.out.println("No recipes to display for: " + listTitle);
            return;
        }
        boolean backToMenu = false;
        while (!backToMenu) {
            System.out.println("\n" + listTitle + ":");
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                System.out.println((i + 1) + ". " + recipe.getTitle() +
                        " (Category: " + (recipe.getCategory() != null ? recipe.getCategory() : "N/A") +
                        ", Cooking time: " + (recipe.getCookingTime() != null ? recipe.getCookingTime() + " min" : "N/A") + ")");
            }
            System.out.println((recipes.size() + 1) + ". Back");
            System.out.print("Select a recipe to view details (or " + (recipes.size() + 1) + " to go back): ");
            String choiceStr = scanner.nextLine();

            try {
                int choice = Integer.parseInt(choiceStr);
                if (choice > 0 && choice <= recipes.size()) {
                    showRecipeDetails(recipes.get(choice - 1));
                } else if (choice == recipes.size() + 1) {
                    backToMenu = true;
                } else {
                    System.out.println("Invalid selection. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    /**
     * Displays recipes belonging to a specific category.
     *
     * @param category The name of the category whose recipes are to be displayed.
     */
    private void showRecipesByCategory(String category) {
        List<Recipe> recipes = recipeService.searchRecipesByCategory(category);
        if (recipes.isEmpty()) {
            System.out.println("No recipes found in category: " + category);
            return;
        }
        displayRecipeList("Recipes in category: " + category, recipes);
    }

    /**
     * Displays the detailed information of a single recipe.
     *
     * @param recipe The {@link Recipe} object whose details are to be displayed. Must not be {@code null}.
     */
    private void showRecipeDetails(Recipe recipe) {
        if (recipe == null) {
            System.out.println("Error: Cannot display details for a null recipe.");
            return;
        }
        System.out.println("\n=== " + recipe.getTitle() + " ===");
        System.out.println("ID: " + recipe.getId());
        System.out.println("Category: " + (recipe.getCategory() != null ? recipe.getCategory() : "N/A"));
        System.out.println("Cooking Time: " + (recipe.getCookingTime() != null ? recipe.getCookingTime() + " minutes" : "N/A"));
        System.out.println("Created By: " + (recipe.getCreatedBy() != null ? recipe.getCreatedBy() : "N/A"));

        System.out.println("\nIngredients:");
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            for (String ingredient : recipe.getIngredients()) {
                System.out.println("- " + ingredient);
            }
        } else {
            System.out.println("No ingredients listed.");
        }

        System.out.println("\nInstructions:");
        System.out.println(recipe.getInstructions() != null ? recipe.getInstructions() : "No instructions provided.");

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Handles the admin authentication process.
     * <p>
     * Prompts for a password and grants access to the admin menu upon successful authentication.
     * Allows a limited number of attempts.
     * </p>
     */
    private void adminAuthentication() {
        System.out.println("\nAdmin Authentication");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter password (or type 'exit' to cancel): ");
            String password = scanner.nextLine();

            if ("exit".equalsIgnoreCase(password)) {
                return; // Return to previous menu
            }

            if (ADMIN_PASSWORD.equals(password)) {
                adminMenu();
                return; // Exit authentication process after admin session
            } else {
                attempts++;
                int remainingAttempts = MAX_ATTEMPTS - attempts;
                if (remainingAttempts > 0) {
                    System.out.println("Incorrect password. " + remainingAttempts + " attempts remaining.");
                } else {
                    System.out.println("Too many failed attempts. Returning to main menu.");
                }
            }
        }
    }

    /**
     * Displays the admin menu, providing options for managing recipes.
     * <p>
     * Options include viewing all recipes, adding, editing, deleting recipes,
     * and searching recipes.
     * </p>
     */
    private void adminMenu() {
        System.out.println("\nWelcome, Admin!");
        boolean backToMain = false;
        while (!backToMain) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View All Recipes");
            System.out.println("2. Add New Recipe");
            System.out.println("3. Edit Recipe");
            System.out.println("4. Delete Recipe");
            System.out.println("5. Search Recipes (same as client search)");
            System.out.println("6. Back to Main Menu");
            System.out.print("Your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewAllRecipes();
                    break;
                case "2":
                    addNewRecipe();
                    break;
                case "3":
                    editRecipe();
                    break;
                case "4":
                    deleteRecipe();
                    break;
                case "5":
                    searchRecipesMenu(); // Reuses client search functionality
                    break;
                case "6":
                    backToMain = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays all recipes in the system for the admin.
     */
    private void viewAllRecipes() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();
        if (allRecipes.isEmpty()) {
            System.out.println("No recipes found in the database.");
            return;
        }
        displayRecipeList("All Recipes (Admin View)", allRecipes);
    }

    /**
     * Guides the admin through the process of adding a new recipe.
     * <p>
     * Prompts for title, category, cooking time, ingredients, and instructions.
     * The recipe is created with "admin" as the creator.
     * </p>
     */
    private void addNewRecipe() {
        System.out.println("\nAdd New Recipe:");
        Recipe newRecipe = new Recipe();

        System.out.print("Title: ");
        newRecipe.setTitle(scanner.nextLine().trim());

        System.out.print("Category (e.g., Dessert, Main Course): ");
        newRecipe.setCategory(scanner.nextLine().trim());

        System.out.print("Cooking Time (minutes): ");
        try {
            int cookingTime = Integer.parseInt(scanner.nextLine().trim());
            if (cookingTime < 0) {
                System.out.println("Cooking time cannot be negative. Setting to 0.");
                newRecipe.setCookingTime(0);
            } else {
                newRecipe.setCookingTime(cookingTime);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid cooking time format. Setting to 0.");
            newRecipe.setCookingTime(0);
        }

        System.out.println("Ingredients (enter an empty line to finish):");
        List<String> ingredients = new ArrayList<>();
        while (true) {
            System.out.print("- ");
            String ingredient = scanner.nextLine().trim();
            if (ingredient.isEmpty()) {
                break;
            }
            ingredients.add(ingredient);
        }
        newRecipe.setIngredients(ingredients);

        System.out.println("Instructions:");
        newRecipe.setInstructions(scanner.nextLine()); // Instructions can be multi-line if scanner setup allows

        try {
            Recipe saved = recipeService.createRecipe(newRecipe, "admin"); // "admin" is the creator
            System.out.println("Recipe '" + saved.getTitle() + "' saved successfully! ID: " + saved.getId());
        } catch (Exception e) {
            System.err.println("Error saving recipe: " + e.getMessage());
            // e.printStackTrace(); // For debugging
        }
    }

    /**
     * Guides the admin through editing an existing recipe.
     * <p>
     * The admin selects a recipe from a list, then can modify its fields.
     * Empty input for a field keeps its current value.
     * </p>
     */
    private void editRecipe() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();
        if (allRecipes.isEmpty()) {
            System.out.println("No recipes available to edit.");
            return;
        }

        displayRecipeList("Select a recipe to edit", allRecipes);
        System.out.print("Enter the number of the recipe to edit (or 0 or other invalid to cancel): ");
        String choiceStr = scanner.nextLine();
        int recipeIndex;

        try {
            recipeIndex = Integer.parseInt(choiceStr) - 1;
            if (recipeIndex < 0 || recipeIndex >= allRecipes.size()) {
                System.out.println("Edit cancelled or invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Edit cancelled.");
            return;
        }

        Recipe recipeToEdit = allRecipes.get(recipeIndex);
        Recipe detailsToUpdate = new Recipe(); // Use a new object to hold updates
        detailsToUpdate.setId(recipeToEdit.getId()); // Critical: ID must be set for update
        detailsToUpdate.setCreatedBy(recipeToEdit.getCreatedBy()); // Preserve original creator

        System.out.println("\nEditing Recipe: " + recipeToEdit.getTitle() + " (ID: " + recipeToEdit.getId() + ")");
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New Title [" + recipeToEdit.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        detailsToUpdate.setTitle(title.isEmpty() ? recipeToEdit.getTitle() : title);

        System.out.print("New Category [" + recipeToEdit.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        detailsToUpdate.setCategory(category.isEmpty() ? recipeToEdit.getCategory() : category);

        System.out.print("New Cooking Time (minutes) [" + recipeToEdit.getCookingTime() + "]: ");
        String cookingTimeStr = scanner.nextLine().trim();
        if (!cookingTimeStr.isEmpty()) {
            try {
                int ct = Integer.parseInt(cookingTimeStr);
                detailsToUpdate.setCookingTime(ct >=0 ? ct : recipeToEdit.getCookingTime());
            } catch (NumberFormatException e) {
                System.out.println("Invalid cooking time format. Keeping current value: " + recipeToEdit.getCookingTime());
                detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
            }
        } else {
            detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
        }

        System.out.println("Current Ingredients: " + (recipeToEdit.getIngredients() != null ? String.join(", ", recipeToEdit.getIngredients()) : "None"));
        System.out.print("Modify ingredients? (y/n) [n]: ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Enter new ingredients (empty line to finish):");
            List<String> ingredients = new ArrayList<>();
            while (true) {
                System.out.print("- ");
                String ingredient = scanner.nextLine().trim();
                if (ingredient.isEmpty()) break;
                ingredients.add(ingredient);
            }
            detailsToUpdate.setIngredients(ingredients.isEmpty() ? recipeToEdit.getIngredients() : ingredients);
        } else {
            detailsToUpdate.setIngredients(recipeToEdit.getIngredients());
        }

        System.out.println("Current Instructions: " + recipeToEdit.getInstructions());
        System.out.print("New Instructions (press Enter to keep current): ");
        String instructions = scanner.nextLine(); // No trim, allow leading/trailing spaces if intended
        detailsToUpdate.setInstructions(instructions.isEmpty() ? recipeToEdit.getInstructions() : instructions);

        try {
            Recipe updated = recipeService.updateRecipe(recipeToEdit.getId(), detailsToUpdate);
            System.out.println("Recipe '" + updated.getTitle() + "' updated successfully!");
        } catch (Exception e) { // Catch RecipeNotFoundException or general exceptions
            System.err.println("Error updating recipe: " + e.getMessage());
            // e.printStackTrace(); // For debugging
        }
    }

    /**
     * Guides the admin through deleting an existing recipe.
     * <p>
     * The admin selects a recipe from a list and confirms the deletion.
     * </p>
     */
    private void deleteRecipe() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();
        if (allRecipes.isEmpty()) {
            System.out.println("No recipes available to delete.");
            return;
        }

        displayRecipeList("Select a recipe to delete", allRecipes);
        System.out.print("Enter the number of the recipe to delete (or 0 or other invalid to cancel): ");
        String choiceStr = scanner.nextLine();
        int recipeIndex;

        try {
            recipeIndex = Integer.parseInt(choiceStr) - 1;
            if (recipeIndex < 0 || recipeIndex >= allRecipes.size()) {
                System.out.println("Delete cancelled or invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Delete cancelled.");
            return;
        }

        Recipe recipeToDelete = allRecipes.get(recipeIndex);
        System.out.println("\nAre you sure you want to delete the recipe: '" + recipeToDelete.getTitle() + "' (ID: " + recipeToDelete.getId() + ")? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y")) {
            try {
                recipeService.deleteRecipe(recipeToDelete.getId());
                System.out.println("Recipe '" + recipeToDelete.getTitle() + "' deleted successfully!");
            } catch (Exception e) { // Catch RecipeNotFoundException or general exceptions
                System.err.println("Error deleting recipe: " + e.getMessage());
                // e.printStackTrace(); // For debugging
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}