package com.example.recipeoop_1;

import com.example.recipeoop_1.exception.RecipeNotFoundException;
import com.example.recipeoop_1.model.Recipe;
import com.example.recipeoop_1.service.CategoryService;
import com.example.recipeoop_1.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
 * @author Elie Issa - Michel Ghazaly (Original Authors), Your Name/Team Name (Maintainers)
 * @version 1.2
 * @since 2025-05-14
 */
@SpringBootApplication
@EnableMongoRepositories // Enables Spring Data MongoDB repositories
public class RecipeOop1Application {

    private static final Logger log = LoggerFactory.getLogger(RecipeOop1Application.class);

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
        log.info("RecipeOOP_1 Application started.");

        Environment env = context.getEnvironment();
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        if (isProd) {
            log.info("Application running in 'prod' profile. ConsoleUI will not be started.");
            // In production, the application will continue running to serve web requests.
            // The context is not closed here.
        } else {
            log.info("Application running in non-'prod' profile. Attempting to start ConsoleUI.");
            try {
                ConsoleUI consoleUI = context.getBean(ConsoleUI.class);
                consoleUI.start(); // Start the interactive console UI
                log.info("ConsoleUI finished.");
            } catch (Exception e) {
                log.error("Failed to start or run ConsoleUI: {}", e.getMessage(), e);
            } finally {
                // For a CLI application that should exit after the ConsoleUI is done,
                // closing the context is appropriate in non-prod environments.
                log.info("Closing application context for non-prod environment.");
                context.close();
                log.info("Application context closed.");
            }
        }
    }
}

/**
 * A Spring {@link Component} that provides a console-based user interface for the
 * Recipe Management System.
 * <p>
 * This UI is primarily intended for local development, testing, and demonstration purposes.
 * It allows users to interact with the recipe service through a series of text-based menus
 * for operations like viewing, adding, editing, deleting, and searching recipes.
 * The {@link #start()} method is manually called from the main application
 * class when not in a "prod" environment.
 * </p>
 * It implements {@link CommandLineRunner} mainly as a convention, though its {@code run}
 * method is not the primary entry point for the UI logic in this setup.
 *
 * @see RecipeService
 * @see CategoryService
 */
@Component
class ConsoleUI implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ConsoleUI.class);
    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final Scanner scanner;

    /**
     * The password required for accessing admin functionalities in the console UI.
     * Hardcoded for simplicity in this demonstration UI.
     * WARNING: This is not secure for any real-world application.
     */
    private static final String ADMIN_PASSWORD = "1234"; // TODO: Externalize or remove for production-like setup

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
        // This method is part of CommandLineRunner, but we use start() for explicit control.
        log.debug("ConsoleUI.run() called, but UI is started via start() method.");
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
        log.info("ConsoleUI started. Welcome to Recipe Management System!");

        boolean exit = false;
        while (!exit) {
            System.out.println("\nPlease select an option:");
            System.out.println("1. Login as Client");
            System.out.println("2. Login as Admin");
            System.out.println("3. Exit");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    clientMenu();
                    break;
                case "2":
                    adminAuthentication();
                    break;
                case "3":
                    exit = true;
                    log.info("Exiting ConsoleUI. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 3.");
            }
        }
        scanner.close(); // Close the scanner when the UI exits
        log.info("Scanner closed.");
    }

    private void clientMenu() {
        boolean backToMain = false;
        while (!backToMain) {
            List<String> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println("\nNo recipe categories found in the database.");
                System.out.println("You can still search for recipes or go back.");
                System.out.println("1. Search for recipes");
                System.out.println("2. Back to main menu");
                System.out.print("Your choice: ");
                String choice = scanner.nextLine().trim();
                if ("1".equals(choice)) {
                    searchRecipesMenu("client");
                }
                backToMain = true; // Exit client menu after this path
                continue;
            }

            System.out.println("\nAvailable Categories:");
            for (int i = 0; i < categories.size(); i++) {
                // Category names from service are already clean (without "recipe_")
                System.out.println((i + 1) + ". " + categories.get(i));
            }
            System.out.println((categories.size() + 1) + ". Search for recipes");
            System.out.println((categories.size() + 2) + ". Back to main menu");
            System.out.print("Select a category or option: ");
            String choice = scanner.nextLine().trim();

            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= categories.size()) {
                    String selectedCategory = categories.get(numericChoice - 1);
                    showRecipesByCategory(selectedCategory, "client");
                } else if (numericChoice == categories.size() + 1) {
                    searchRecipesMenu("client");
                } else if (numericChoice == categories.size() + 2) {
                    backToMain = true;
                } else {
                    System.out.println("Invalid choice. Please select a number from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void searchRecipesMenu(String userRole) { // userRole can be "client" or "admin"
        boolean back = false;
        while (!back) {
            System.out.println("\nSearch Options:");
            System.out.println("1. Search by title");
            System.out.println("2. Search by cooking time (max minutes)");
            System.out.println("3. Search by ingredient");
            System.out.println("4. Advanced search (multiple criteria)");
            System.out.println("5. Back");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim();
            List<Recipe> searchResults = new ArrayList<>();
            boolean performDisplay = true;

            try {
                switch (choice) {
                    case "1":
                        System.out.print("Enter title keyword: ");
                        String title = scanner.nextLine().trim();
                        searchResults = recipeService.searchRecipesByTitle(title);
                        break;
                    case "2":
                        System.out.print("Enter maximum cooking time (minutes): ");
                        int cookingTime = Integer.parseInt(scanner.nextLine().trim());
                        searchResults = recipeService.searchRecipesByCookingTime(cookingTime);
                        break;
                    case "3":
                        System.out.print("Enter ingredient keyword: ");
                        String ingredient = scanner.nextLine().trim();
                        searchResults = recipeService.searchRecipesByIngredient(ingredient);
                        break;
                    case "4":
                        advancedSearchMenu(userRole);
                        performDisplay = false; // Advanced search handles its own display
                        break;
                    case "5":
                        back = true;
                        performDisplay = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        performDisplay = false;
                }

                if (performDisplay) {
                    if (searchResults.isEmpty()) {
                        System.out.println("No recipes found matching your search criteria.");
                    } else {
                        displayRecipeList("Search Results", searchResults, userRole);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please try again.");
            } catch (Exception e) {
                log.error("Error during recipe search: {}", e.getMessage(), e);
                System.out.println("An error occurred while searching for recipes. Please try again.");
            }
        }
    }

    private void advancedSearchMenu(String userRole) {
        System.out.println("\nAdvanced Search (leave blank or press Enter to skip a criterion):");

        System.out.print("Title contains: ");
        String title = scanner.nextLine().trim();

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        Integer cookingTime = null;
        System.out.print("Maximum cooking time (minutes): ");
        String cookingTimeStr = scanner.nextLine().trim();
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

        try {
            List<Recipe> searchResults = recipeService.advancedSearch(
                    title.isEmpty() ? null : title,
                    category.isEmpty() ? null : category,
                    cookingTime,
                    ingredient.isEmpty() ? null : ingredient
            );

            if (searchResults.isEmpty()) {
                System.out.println("No recipes found matching your advanced search criteria.");
            } else {
                displayRecipeList("Advanced Search Results", searchResults, userRole);
            }
        } catch (Exception e) {
            log.error("Error during advanced recipe search: {}", e.getMessage(), e);
            System.out.println("An error occurred during advanced search. Please try again.");
        }
    }

    private void displayRecipeList(String listTitle, List<Recipe> recipes, String userRole) {
        if (recipes == null || recipes.isEmpty()) {
            System.out.println("\nNo recipes to display for: " + listTitle);
            return;
        }
        boolean backToMenu = false;
        while (!backToMenu) {
            System.out.println("\n--- " + listTitle + " ---");
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                System.out.printf("%d. %s (Category: %s, Cooking time: %s min, Created by: %s)\n",
                        (i + 1),
                        recipe.getTitle(),
                        recipe.getCategory() != null ? recipe.getCategory() : "N/A",
                        recipe.getCookingTime() != null ? recipe.getCookingTime().toString() : "N/A",
                        recipe.getCreatedBy() != null ? recipe.getCreatedBy() : "N/A");
            }
            System.out.println((recipes.size() + 1) + ". Back");
            System.out.print("Select a recipe to view details (or " + (recipes.size() + 1) + " to go back): ");
            String choiceStr = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(choiceStr);
                if (choice > 0 && choice <= recipes.size()) {
                    showRecipeDetails(recipes.get(choice - 1));
                    // After showing details, if the user is admin, offer edit/delete options for THIS recipe
                    if ("admin".equals(userRole)) {
                        // This is a good place to ask if the admin wants to edit or delete the viewed recipe.
                        // For simplicity in this refactor, it returns to the list.
                        // Example prompt:
                        // System.out.println("Admin actions for '" + recipes.get(choice - 1).getTitle() + "': (e)dit, (d)elete, or (b)ack to list?");
                        // String adminAction = scanner.nextLine().trim();
                        // // Handle adminAction...
                    }
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

    private void showRecipesByCategory(String category, String userRole) {
        System.out.println("\nFetching recipes in category: " + category);
        try {
            List<Recipe> recipes = recipeService.searchRecipesByCategory(category);
            if (recipes.isEmpty()) {
                System.out.println("No recipes found in category: " + category);
            } else {
                displayRecipeList("Recipes in category: " + category, recipes, userRole);
            }
        } catch (Exception e) {
            log.error("Error fetching recipes by category '{}': {}", category, e.getMessage(), e);
            System.out.println("An error occurred while fetching recipes. Please try again.");
        }
    }

    private void showRecipeDetails(Recipe recipe) {
        if (recipe == null) {
            log.warn("Attempted to show details for a null recipe.");
            System.out.println("Error: Recipe details are not available.");
            return;
        }
        System.out.println("\n=== " + recipe.getTitle() + " ===");
        System.out.println("ID: " + recipe.getId());
        System.out.println("Category: " + (recipe.getCategory() != null ? recipe.getCategory() : "N/A"));
        System.out.println("Cooking Time: " + (recipe.getCookingTime() != null ? recipe.getCookingTime() + " minutes" : "N/A"));
        System.out.println("Created By: " + (recipe.getCreatedBy() != null ? recipe.getCreatedBy() : "N/A"));

        System.out.println("\nIngredients:");
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            recipe.getIngredients().forEach(ingredient -> System.out.println("- " + ingredient));
        } else {
            System.out.println("No ingredients listed.");
        }

        System.out.println("\nInstructions:");
        System.out.println(recipe.getInstructions() != null ? recipe.getInstructions() : "No instructions provided.");

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void adminAuthentication() {
        System.out.println("\n--- Admin Authentication ---");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter password (or type 'back' to cancel): ");
            String password = scanner.nextLine().trim();

            if ("back".equalsIgnoreCase(password)) {
                return;
            }

            if (ADMIN_PASSWORD.equals(password)) {
                log.info("Admin authentication successful.");
                adminMenu();
                return;
            } else {
                attempts++;
                int remainingAttempts = MAX_ATTEMPTS - attempts;
                if (remainingAttempts > 0) {
                    System.out.println("Incorrect password. " + remainingAttempts + " attempts remaining.");
                } else {
                    log.warn("Admin authentication failed after {} attempts.", MAX_ATTEMPTS);
                    System.out.println("Too many failed attempts. Returning to main menu.");
                }
            }
        }
    }

    private void adminMenu() {
        System.out.println("\nWelcome, Admin!");
        boolean backToMain = false;
        while (!backToMain) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. View All Recipes");
            System.out.println("2. Add New Recipe");
            System.out.println("3. Edit Recipe");
            System.out.println("4. Delete Recipe");
            System.out.println("5. Search Recipes");
            System.out.println("6. Back to Main Menu");
            System.out.print("Your choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewAllRecipesForAdmin();
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
                    searchRecipesMenu("admin");
                    break;
                case "6":
                    backToMain = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a number from 1 to 6.");
            }
        }
    }

    private void viewAllRecipesForAdmin() {
        try {
            List<Recipe> allRecipes = recipeService.getAllRecipes();
            if (allRecipes.isEmpty()) {
                System.out.println("No recipes found in the database.");
            } else {
                displayRecipeList("All Recipes (Admin View)", allRecipes, "admin");
            }
        } catch (Exception e) {
            log.error("Error fetching all recipes for admin: {}", e.getMessage(), e);
            System.out.println("An error occurred while fetching recipes. Please check logs.");
        }
    }

    private void addNewRecipe() {
        System.out.println("\n--- Add New Recipe ---");
        Recipe newRecipe = new Recipe();

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty. Aborting add recipe.");
            return;
        }
        newRecipe.setTitle(title);

        System.out.print("Category (e.g., Dessert, Main Course - press Enter for 'uncategorized'): ");
        String category = scanner.nextLine().trim();
        newRecipe.setCategory(category.isEmpty() ? "uncategorized" : category);

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

        List<String> ingredients = new ArrayList<>();
        System.out.println("Ingredients (enter each ingredient on a new line, then an empty line to finish):");
        while (true) {
            System.out.print("- ");
            String ingredient = scanner.nextLine().trim();
            if (ingredient.isEmpty()) {
                if (ingredients.isEmpty()) { // Check if ingredients list is still empty
                    System.out.println("At least one ingredient is required. Please add an ingredient or type 'cancel' to abort.");
                    System.out.print("- ");
                    ingredient = scanner.nextLine().trim(); // Give one more chance
                    if (ingredient.isEmpty() || "cancel".equalsIgnoreCase(ingredient)) {
                        System.out.println("Adding recipe aborted due to no ingredients.");
                        return;
                    }
                    // If they entered something, add it before breaking or continuing loop
                    if (!ingredient.isEmpty()) ingredients.add(ingredient); else break;
                } else { // Ingredients list is not empty, so empty line means done
                    break;
                }
            } else {
                ingredients.add(ingredient);
            }
        }

        if (ingredients.isEmpty()) { // Final check
            System.out.println("No ingredients provided. Aborting add recipe.");
            return;
        }
        newRecipe.setIngredients(ingredients);


        System.out.println("Instructions (type instructions and press Enter):");
        String instructions = scanner.nextLine().trim();
        if (instructions.isEmpty()){
            System.out.println("Instructions cannot be empty. Aborting add recipe.");
            return;
        }
        newRecipe.setInstructions(instructions);

        try {
            Recipe saved = recipeService.createRecipe(newRecipe, "admin"); // "admin" is the creator
            log.info("Admin created new recipe: '{}', ID: {}", saved.getTitle(), saved.getId());
            System.out.println("Recipe '" + saved.getTitle() + "' saved successfully! ID: " + saved.getId());
        } catch (Exception e) {
            log.error("Error saving new recipe by admin: {}", e.getMessage(), e);
            System.err.println("Error saving recipe: " + e.getMessage() + ". Please check logs.");
        }
    }

    private void editRecipe() {
        System.out.println("\n--- Edit Recipe ---");
        List<Recipe> allRecipes;
        try {
            allRecipes = recipeService.getAllRecipes();
        } catch (Exception e) {
            log.error("Failed to retrieve recipes for editing: {}", e.getMessage(), e);
            System.out.println("Could not retrieve recipes. Please try again.");
            return;
        }

        if (allRecipes.isEmpty()) {
            System.out.println("No recipes available to edit.");
            return;
        }

        System.out.println("Select a recipe to edit:");
        for (int i = 0; i < allRecipes.size(); i++) {
            System.out.printf("%d. %s (ID: %s)\n", (i + 1), allRecipes.get(i).getTitle(), allRecipes.get(i).getId());
        }
        System.out.println((allRecipes.size() + 1) + ". Cancel");
        System.out.print("Enter your choice: ");
        String choiceStr = scanner.nextLine().trim();
        int recipeIdxChoice;

        try {
            recipeIdxChoice = Integer.parseInt(choiceStr);
            if (recipeIdxChoice == allRecipes.size() + 1) {
                System.out.println("Edit cancelled.");
                return;
            }
            if (recipeIdxChoice <= 0 || recipeIdxChoice > allRecipes.size()) {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number. Edit cancelled.");
            return;
        }

        Recipe recipeToEdit = allRecipes.get(recipeIdxChoice - 1);
        Recipe detailsToUpdate = new Recipe();

        System.out.println("\nEditing Recipe: " + recipeToEdit.getTitle() + " (ID: " + recipeToEdit.getId() + ")");
        System.out.println("(Press Enter to keep current value, type new value to change)");

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
                detailsToUpdate.setCookingTime(ct >= 0 ? ct : recipeToEdit.getCookingTime());
            } catch (NumberFormatException e) {
                System.out.println("Invalid cooking time format. Keeping current value: " + recipeToEdit.getCookingTime());
                detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
            }
        } else {
            detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
        }

        System.out.println("Current Ingredients: " + (recipeToEdit.getIngredients() != null ? String.join(", ", recipeToEdit.getIngredients()) : "None"));
        System.out.print("Modify ingredients? (y/n) [n]: ");

        List<String> newIngredientsList = null;

        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            newIngredientsList = new ArrayList<>();
            System.out.println("Enter new ingredients (each on a new line, empty line to finish):");
            while (true) {
                System.out.print("- ");
                String ingredient = scanner.nextLine().trim();
                if (ingredient.isEmpty()) {
                    if (newIngredientsList.isEmpty()) {
                        System.out.println("Ingredients list cannot be empty if you choose to modify. Keeping original ingredients.");
                        newIngredientsList.addAll(recipeToEdit.getIngredients());
                    }
                    break;
                }
                newIngredientsList.add(ingredient);
            }
            detailsToUpdate.setIngredients(newIngredientsList);
        } else {
            detailsToUpdate.setIngredients(recipeToEdit.getIngredients());
        }

        System.out.println("Current Instructions:\n" + recipeToEdit.getInstructions());
        System.out.print("New Instructions (press Enter to keep current, or type new instructions): ");
        String instructions = scanner.nextLine();
        detailsToUpdate.setInstructions(instructions.isEmpty() ? recipeToEdit.getInstructions() : instructions);

        try {
            Recipe updated = recipeService.updateRecipe(recipeToEdit.getId(), detailsToUpdate);
            log.info("Admin updated recipe ID {}: new title '{}'", updated.getId(), updated.getTitle());
            System.out.println("Recipe '" + updated.getTitle() + "' updated successfully!");
        } catch (RecipeNotFoundException e) {
            log.warn("Recipe with ID {} not found during update attempt by admin.", recipeToEdit.getId());
            System.err.println(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating recipe ID {}: {}", recipeToEdit.getId(), e.getMessage(), e);
            System.err.println("Error updating recipe: " + e.getMessage() + ". Please check logs.");
        }
    }

    private void deleteRecipe() {
        System.out.println("\n--- Delete Recipe ---");
        List<Recipe> allRecipes;
        try {
            allRecipes = recipeService.getAllRecipes();
        } catch (Exception e) {
            log.error("Failed to retrieve recipes for deletion: {}", e.getMessage(), e);
            System.out.println("Could not retrieve recipes. Please try again.");
            return;
        }

        if (allRecipes.isEmpty()) {
            System.out.println("No recipes available to delete.");
            return;
        }

        System.out.println("Select a recipe to delete:");
        for (int i = 0; i < allRecipes.size(); i++) {
            System.out.printf("%d. %s (ID: %s)\n", (i + 1), allRecipes.get(i).getTitle(), allRecipes.get(i).getId());
        }
        System.out.println((allRecipes.size() + 1) + ". Cancel");
        System.out.print("Enter your choice: ");
        String choiceStr = scanner.nextLine().trim();
        int recipeIdxChoice;

        try {
            recipeIdxChoice = Integer.parseInt(choiceStr);
            if (recipeIdxChoice == allRecipes.size() + 1) {
                System.out.println("Delete cancelled.");
                return;
            }
            if (recipeIdxChoice <= 0 || recipeIdxChoice > allRecipes.size()) {
                System.out.println("Invalid selection.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number. Delete cancelled.");
            return;
        }

        Recipe recipeToDelete = allRecipes.get(recipeIdxChoice - 1);
        System.out.println("\nAre you sure you want to delete the recipe: '" + recipeToDelete.getTitle() + "' (ID: " + recipeToDelete.getId() + ")? (y/n): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y")) {
            try {
                recipeService.deleteRecipe(recipeToDelete.getId());
                log.info("Admin deleted recipe ID {}: '{}'", recipeToDelete.getId(), recipeToDelete.getTitle());
                System.out.println("Recipe '" + recipeToDelete.getTitle() + "' deleted successfully!");
            } catch (RecipeNotFoundException e) {
                log.warn("Recipe with ID {} not found during delete attempt by admin.", recipeToDelete.getId());
                System.err.println(e.getMessage());
            } catch (Exception e) {
                log.error("Error deleting recipe ID {}: {}", recipeToDelete.getId(), e.getMessage(), e);
                System.err.println("Error deleting recipe: " + e.getMessage() + ". Please check logs.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
}