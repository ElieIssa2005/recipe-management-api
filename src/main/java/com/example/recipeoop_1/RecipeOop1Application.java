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
// import java.util.InputMismatchException; // Not directly used, can be removed if not needed for other parts

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
 * @version 1.3
 * @since 2025-05-17
 */
@SpringBootApplication
@EnableMongoRepositories // Enables Spring Data MongoDB repositories
public class RecipeOop1Application {

    /**
     * Logger for this class.
     */
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
        } else {
            log.info("Application running in non-'prod' profile. Attempting to start ConsoleUI.");
            try {
                ConsoleUI consoleUI = context.getBean(ConsoleUI.class);
                consoleUI.start(); // Start the interactive console UI
                log.info("ConsoleUI finished.");
            } catch (Exception e) {
                log.error("Failed to start or run ConsoleUI: {}", e.getMessage(), e);
            } finally {
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
 */
@Component
class ConsoleUI implements CommandLineRunner {
    /**
     * Logger for the ConsoleUI class.
     */
    private static final Logger log = LoggerFactory.getLogger(ConsoleUI.class);
    /**
     * Service for recipe-related operations.
     */
    private final RecipeService recipeService;
    /**
     * Service for category-related operations.
     */
    private final CategoryService categoryService;
    /**
     * Scanner for reading user input from the console.
     */
    private final Scanner scanner;

    /**
     * The password required for accessing admin functionalities in the console UI.
     * Hardcoded for simplicity in this demonstration UI.
     * WARNING: This is not secure for any real-world application.
     */
    private static final String ADMIN_PASSWORD = "1234"; // TODO: Externalize

    /** ANSI escape code to reset console color. */
    public static final String ANSI_RESET = "\u001B[0m";
    /** ANSI escape code for cyan color. */
    public static final String ANSI_CYAN = "\u001B[36m";
    /** ANSI escape code for yellow color. */
    public static final String ANSI_YELLOW = "\u001B[33m";
    /** ANSI escape code for green color. */
    public static final String ANSI_GREEN = "\u001B[32m";
    /** ANSI escape code for red color. */
    public static final String ANSI_RED = "\u001B[31m";
    /** ANSI escape code for blue color. */
    public static final String ANSI_BLUE = "\u001B[34m";
    /** ANSI escape code for bold text. */
    public static final String ANSI_BOLD = "\u001B[1m";


    /**
     * Constructs a {@code ConsoleUI} with necessary service dependencies.
     *
     * @param recipeService The {@link RecipeService} for recipe-related operations.
     * @param categoryService The {@link CategoryService} for category-related operations.
     */
    public ConsoleUI(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Callback method from {@link CommandLineRunner}.
     * Intentionally left empty as UI is started via {@link #start()}.
     * @param args Incoming command-line arguments, not used.
     */
    @Override
    public void run(String... args) {
        log.debug("ConsoleUI.run() called, but UI is started via start() method.");
    }

    /**
     * Starts the main loop of the console-based user interface.
     */
    public void start() {
        displayWelcomeMessage();
        boolean exit = false;
        while (!exit) {
            displayMainMenu();
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
                    System.out.println(ANSI_YELLOW + "Exiting Recipe Management System. Goodbye!" + ANSI_RESET);
                    log.info("Exiting ConsoleUI. Goodbye!");
                    break;
                default:
                    System.out.println(ANSI_RED + "Invalid choice. Please enter a number from 1 to 3." + ANSI_RESET);
            }
        }
        scanner.close();
        log.info("Scanner closed.");
    }

    /**
     * Displays the welcome message to the console.
     */
    private void displayWelcomeMessage() {
        System.out.println(ANSI_BOLD + ANSI_CYAN);
        System.out.println("***************************************************");
        System.out.println("* *");
        System.out.println("* Welcome to the Recipe Management System    *");
        System.out.println("* *");
        System.out.println("***************************************************" + ANSI_RESET);
    }

    /**
     * Displays the main menu options.
     */
    private void displayMainMenu() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Main Menu ---" + ANSI_RESET);
        System.out.println("1. Login as Client");
        System.out.println("2. Login as Admin");
        System.out.println("3. Exit");
        System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);
    }

    /**
     * Handles the client user menu and interactions.
     */
    private void clientMenu() {
        boolean backToMain = false;
        while (!backToMain) {
            System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Client Menu ---" + ANSI_RESET);
            List<String> categories = categoryService.getAllCategories();
            if (categories.isEmpty()) {
                System.out.println(ANSI_YELLOW + "No recipe categories found." + ANSI_RESET);
                System.out.println("1. Search for recipes");
                System.out.println("2. Back to main menu");
                System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);
                String choice = scanner.nextLine().trim();
                if ("1".equals(choice)) {
                    searchRecipesMenu("client");
                }
                backToMain = true;
                continue;
            }

            System.out.println(ANSI_GREEN + "Available Categories:" + ANSI_RESET);
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i));
            }
            System.out.println((categories.size() + 1) + ". Search for recipes");
            System.out.println((categories.size() + 2) + ". Back to main menu");
            System.out.print(ANSI_YELLOW + "Select a category or option: " + ANSI_RESET);
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
                    System.out.println(ANSI_RED + "Invalid choice. Please select a number from the list." + ANSI_RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET);
            }
        }
    }

    /**
     * Handles the recipe search menu for both clients and admins.
     * @param userRole The role of the current user ("client" or "admin").
     */
    private void searchRecipesMenu(String userRole) {
        boolean back = false;
        while (!back) {
            System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Search Options ---" + ANSI_RESET);
            System.out.println("1. Search by title");
            System.out.println("2. Search by cooking time (max minutes)");
            System.out.println("3. Search by ingredient");
            System.out.println("4. Advanced search (multiple criteria)");
            System.out.println("5. Back");
            System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);

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
                        performDisplay = false;
                        break;
                    case "5":
                        back = true;
                        performDisplay = false;
                        break;
                    default:
                        System.out.println(ANSI_RED + "Invalid choice. Please try again." + ANSI_RESET);
                        performDisplay = false;
                }

                if (performDisplay) {
                    if (searchResults.isEmpty()) {
                        System.out.println(ANSI_YELLOW + "No recipes found matching your search criteria." + ANSI_RESET);
                    } else {
                        displayRecipeList("Search Results", searchResults, userRole);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Invalid numeric input. Please try again." + ANSI_RESET);
            } catch (Exception e) {
                log.error("Error during recipe search: {}", e.getMessage(), e);
                System.out.println(ANSI_RED + "An error occurred while searching for recipes. Please try again." + ANSI_RESET);
            }
        }
    }

    /**
     * Handles the advanced search menu.
     * @param userRole The role of the current user.
     */
    private void advancedSearchMenu(String userRole) {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Advanced Search ---" + ANSI_RESET);
        System.out.println("(Leave blank or press Enter to skip a criterion)");

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
                    System.out.println(ANSI_YELLOW + "Cooking time cannot be negative. Ignoring this criterion." + ANSI_RESET);
                    cookingTime = null;
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_YELLOW + "Invalid cooking time format. Ignoring this criterion." + ANSI_RESET);
            }
        }
        System.out.print("Ingredient contains: ");
        String ingredient = scanner.nextLine().trim();

        try {
            List<Recipe> searchResults = recipeService.advancedSearch(
                    title.isEmpty() ? null : title,
                    category.isEmpty() ? null : category,
                    cookingTime,
                    ingredient.isEmpty() ? null : ingredient);

            if (searchResults.isEmpty()) {
                System.out.println(ANSI_YELLOW + "No recipes found matching your advanced search criteria." + ANSI_RESET);
            } else {
                displayRecipeList("Advanced Search Results", searchResults, userRole);
            }
        } catch (Exception e) {
            log.error("Error during advanced recipe search: {}", e.getMessage(), e);
            System.out.println(ANSI_RED + "An error occurred during advanced search. Please try again." + ANSI_RESET);
        }
    }

    /**
     * Displays a list of recipes and allows selection for viewing details or admin actions.
     * @param listTitle The title for the displayed list.
     * @param recipes The list of recipes to display.
     * @param userRole The role of the current user.
     */
    private void displayRecipeList(String listTitle, List<Recipe> recipes, String userRole) {
        if (recipes == null || recipes.isEmpty()) {
            System.out.println(ANSI_YELLOW + "\nNo recipes to display for: " + listTitle + ANSI_RESET);
            return;
        }
        boolean backToListMenu = false;
        while (!backToListMenu) {
            System.out.println(ANSI_BOLD + ANSI_GREEN + "\n--- " + listTitle + " ---" + ANSI_RESET);
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
            System.out.print(ANSI_YELLOW + "Select a recipe to view details (or " + (recipes.size() + 1) + " to go back): " + ANSI_RESET);
            String choiceStr = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(choiceStr);
                if (choice > 0 && choice <= recipes.size()) {
                    Recipe selectedRecipe = recipes.get(choice - 1);
                    showRecipeDetails(selectedRecipe);
                    if ("admin".equals(userRole)) {
                        adminRecipeActionMenu(selectedRecipe);
                    }
                } else if (choice == recipes.size() + 1) {
                    backToListMenu = true;
                } else {
                    System.out.println(ANSI_RED + "Invalid selection. Please try again." + ANSI_RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET);
            }
        }
    }

    /**
     * Shows recipes for a given category.
     * @param category The category name.
     * @param userRole The role of the current user.
     */
    private void showRecipesByCategory(String category, String userRole) {
        System.out.println(ANSI_GREEN + "\nFetching recipes in category: " + category + ANSI_RESET);
        try {
            List<Recipe> recipes = recipeService.searchRecipesByCategory(category);
            if (recipes.isEmpty()) {
                System.out.println(ANSI_YELLOW + "No recipes found in category: " + category + ANSI_RESET);
            } else {
                displayRecipeList("Recipes in category: " + category, recipes, userRole);
            }
        } catch (Exception e) {
            log.error("Error fetching recipes by category '{}': {}", category, e.getMessage(), e);
            System.out.println(ANSI_RED + "An error occurred while fetching recipes. Please try again." + ANSI_RESET);
        }
    }

    /**
     * Displays the details of a single recipe.
     * @param recipe The recipe to display.
     */
    private void showRecipeDetails(Recipe recipe) {
        if (recipe == null) {
            log.warn("Attempted to show details for a null recipe.");
            System.out.println(ANSI_RED + "Error: Recipe details are not available." + ANSI_RESET);
            return;
        }
        System.out.println(ANSI_BOLD + ANSI_CYAN + "\n=== " + recipe.getTitle() + " ===" + ANSI_RESET);
        System.out.println("ID: " + recipe.getId());
        System.out.println("Category: " + (recipe.getCategory() != null ? recipe.getCategory() : "N/A"));
        System.out.println("Cooking Time: " + (recipe.getCookingTime() != null ? recipe.getCookingTime() + " minutes" : "N/A"));
        System.out.println("Created By: " + (recipe.getCreatedBy() != null ? recipe.getCreatedBy() : "N/A"));

        System.out.println(ANSI_BOLD + "\nIngredients:" + ANSI_RESET);
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            recipe.getIngredients().forEach(ingredient -> System.out.println("- " + ingredient));
        } else {
            System.out.println("No ingredients listed.");
        }

        System.out.println(ANSI_BOLD + "\nInstructions:" + ANSI_RESET);
        System.out.println(recipe.getInstructions() != null ? recipe.getInstructions() : "No instructions provided.");

        // For admin, actions are handled by adminRecipeActionMenu.
        // For client, just a pause.
        // This check is simplified; in a real app, userRole would be passed or obtained from context.
        boolean isAdminViewing = false; // Placeholder for actual role check logic
        if (Thread.currentThread().getStackTrace()[2].getMethodName().contains("admin")) { // very basic check based on call stack
            isAdminViewing = true;
        }

        if (!isAdminViewing) {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    /**
     * Handles admin authentication.
     */
    private void adminAuthentication() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Admin Authentication ---" + ANSI_RESET);
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
                    System.out.println(ANSI_RED + "Incorrect password. " + remainingAttempts + " attempts remaining." + ANSI_RESET);
                } else {
                    log.warn("Admin authentication failed after {} attempts.", MAX_ATTEMPTS);
                    System.out.println(ANSI_RED + "Too many failed attempts. Returning to main menu." + ANSI_RESET);
                }
            }
        }
    }

    /**
     * Displays the main admin menu.
     */
    private void adminMenu() {
        System.out.println(ANSI_GREEN+ "\nWelcome, Admin!" + ANSI_RESET);
        boolean backToMain = false;
        while (!backToMain) {
            System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Admin Menu ---" + ANSI_RESET);
            System.out.println("1. Manage Recipes");
            System.out.println("2. Add New Recipe");
            System.out.println("3. Search Recipes (Global)");
            System.out.println("4. Back to Main Menu");
            System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    adminManageRecipesMenu();
                    break;
                case "2":
                    addNewRecipe();
                    break;
                case "3":
                    searchRecipesMenu("admin");
                    break;
                case "4":
                    backToMain = true;
                    break;
                default:
                    System.out.println(ANSI_RED + "Invalid choice." + ANSI_RESET);
            }
        }
    }

    /**
     * Displays the admin's recipe management options (view by category or search).
     */
    private void adminManageRecipesMenu() {
        boolean backToAdminMenu = false;
        while(!backToAdminMenu) {
            System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Manage Recipes ---" + ANSI_RESET);
            System.out.println("1. View Recipes by Category");
            System.out.println("2. Search All Recipes (then select for actions)");
            System.out.println("3. Back to Admin Menu");
            System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);
            String choice = scanner.nextLine().trim();

            switch(choice) {
                case "1":
                    adminSelectCategoryToViewRecipes();
                    break;
                case "2":
                    searchRecipesMenu("admin");
                    break;
                case "3":
                    backToAdminMenu = true;
                    break;
                default:
                    System.out.println(ANSI_RED + "Invalid choice." + ANSI_RESET);
            }
        }
    }

    /**
     * Allows admin to select a category to view recipes from.
     */
    private void adminSelectCategoryToViewRecipes() {
        List<String> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println(ANSI_YELLOW + "\nNo recipe categories found." + ANSI_RESET);
            return;
        }

        System.out.println(ANSI_GREEN + "\nAvailable Categories:" + ANSI_RESET);
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }
        System.out.println((categories.size() + 1) + ". Back");
        System.out.print(ANSI_YELLOW + "Select a category to view its recipes: " + ANSI_RESET);
        String choiceStr = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(choiceStr);
            if (choice > 0 && choice <= categories.size()) {
                String selectedCategory = categories.get(choice - 1);
                showRecipesByCategory(selectedCategory, "admin");
            } else if (choice == categories.size() + 1) {
                // Go back
            } else {
                System.out.println(ANSI_RED + "Invalid selection." + ANSI_RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET);
        }
    }

    /**
     * Displays action menu for a selected recipe for an admin.
     * @param recipe The recipe to perform actions on.
     */
    private void adminRecipeActionMenu(Recipe recipe) {
        if (recipe == null) return;
        boolean backToList = false;
        while (!backToList) {
            System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Actions for Recipe: " + recipe.getTitle() + " ---" + ANSI_RESET);
            System.out.println("1. Edit Recipe");
            System.out.println("2. Delete Recipe");
            System.out.println("3. Back to previous list");
            System.out.print(ANSI_YELLOW + "Your choice: " + ANSI_RESET);
            String actionChoice = scanner.nextLine().trim();

            switch (actionChoice) {
                case "1":
                    editSelectedRecipe(recipe);
                    backToList = true;
                    break;
                case "2":
                    deleteSelectedRecipe(recipe);
                    backToList = true;
                    break;
                case "3":
                    backToList = true;
                    break;
                default:
                    System.out.println(ANSI_RED + "Invalid action. Please try again." + ANSI_RESET);
            }
        }
    }

    /**
     * Handles adding a new recipe by an admin.
     */
    private void addNewRecipe() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Add New Recipe ---" + ANSI_RESET);
        Recipe newRecipe = new Recipe();

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println(ANSI_RED + "Title cannot be empty. Aborting add recipe." + ANSI_RESET);
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
                System.out.println(ANSI_YELLOW + "Cooking time cannot be negative. Setting to 0." + ANSI_RESET);
                newRecipe.setCookingTime(0);
            } else {
                newRecipe.setCookingTime(cookingTime);
            }
        } catch (NumberFormatException e) {
            System.out.println(ANSI_YELLOW + "Invalid cooking time format. Setting to 0." + ANSI_RESET);
            newRecipe.setCookingTime(0);
        }

        List<String> ingredients = new ArrayList<>();
        System.out.println("Ingredients (enter each ingredient on a new line, then an empty line to finish):");
        while (true) {
            System.out.print("- ");
            String ingredient = scanner.nextLine().trim();
            if (ingredient.isEmpty()) {
                if (ingredients.isEmpty()) {
                    System.out.println(ANSI_RED + "At least one ingredient is required. Please add an ingredient or type 'cancel' to abort." + ANSI_RESET);
                    System.out.print("- ");
                    ingredient = scanner.nextLine().trim();
                    if (ingredient.isEmpty() || "cancel".equalsIgnoreCase(ingredient)) {
                        System.out.println(ANSI_YELLOW + "Adding recipe aborted due to no ingredients." + ANSI_RESET);
                        return;
                    }
                    if (!ingredient.isEmpty()) ingredients.add(ingredient); else break;
                } else {
                    break;
                }
            } else {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            System.out.println(ANSI_RED + "No ingredients provided. Aborting add recipe." + ANSI_RESET);
            return;
        }
        newRecipe.setIngredients(ingredients);

        System.out.println("Instructions (type instructions and press Enter):");
        String instructions = scanner.nextLine().trim();
        if (instructions.isEmpty()){
            System.out.println(ANSI_RED + "Instructions cannot be empty. Aborting add recipe." + ANSI_RESET);
            return;
        }
        newRecipe.setInstructions(instructions);

        try {
            Recipe saved = recipeService.createRecipe(newRecipe, "admin_console");
            log.info("Admin created new recipe: '{}', ID: {}", saved.getTitle(), saved.getId());
            System.out.println(ANSI_GREEN + "Recipe '" + saved.getTitle() + "' saved successfully! ID: " + saved.getId() + ANSI_RESET);
        } catch (Exception e) {
            log.error("Error saving new recipe by admin: {}", e.getMessage(), e);
            System.err.println(ANSI_RED + "Error saving recipe: " + e.getMessage() + ". Please check logs." + ANSI_RESET);
        }
    }

    /**
     * Handles editing a specific, already selected recipe by an admin.
     * @param recipeToEdit The recipe object to be edited.
     */
    private void editSelectedRecipe(Recipe recipeToEdit) {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Editing Recipe: " + recipeToEdit.getTitle() + " (ID: " + recipeToEdit.getId() + ") ---" + ANSI_RESET);
        System.out.println("(Press Enter to keep current value, type new value to change)");

        Recipe detailsToUpdate = new Recipe();

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
                System.out.println(ANSI_YELLOW+"Invalid cooking time format. Keeping current value: " + recipeToEdit.getCookingTime()+ANSI_RESET);
                detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
            }
        } else {
            detailsToUpdate.setCookingTime(recipeToEdit.getCookingTime());
        }

        System.out.println("Current Ingredients: " + (recipeToEdit.getIngredients() != null ? String.join(", ", recipeToEdit.getIngredients()) : "None"));
        System.out.print("Modify ingredients? (y/n) [n]: ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            List<String> newIngredientsList = new ArrayList<>();
            System.out.println("Enter new ingredients (each on a new line, empty line to finish):");
            while (true) {
                System.out.print("- ");
                String ingredient = scanner.nextLine().trim();
                if (ingredient.isEmpty()) {
                    if (newIngredientsList.isEmpty()) {
                        System.out.println(ANSI_YELLOW+"Ingredients list cannot be empty if you choose to modify. Keeping original ingredients."+ANSI_RESET);
                        newIngredientsList.addAll(recipeToEdit.getIngredients());
                    }
                    break;
                }
                newIngredientsList.add(ingredient);
            }
            detailsToUpdate.setIngredients(newIngredientsList);
        } else {
            detailsToUpdate.setIngredients(new ArrayList<>(recipeToEdit.getIngredients()));
        }

        System.out.println("Current Instructions:\n" + recipeToEdit.getInstructions());
        System.out.print("New Instructions (press Enter to keep current, or type new instructions): ");
        String instructions = scanner.nextLine();
        detailsToUpdate.setInstructions(instructions.isEmpty() ? recipeToEdit.getInstructions() : instructions.trim());

        try {
            Recipe updated = recipeService.updateRecipe(recipeToEdit.getId(), detailsToUpdate);
            log.info("Admin updated recipe ID {}: new title '{}'", updated.getId(), updated.getTitle());
            System.out.println(ANSI_GREEN + "Recipe '" + updated.getTitle() + "' updated successfully!" + ANSI_RESET);
        } catch (RecipeNotFoundException e) {
            log.warn("Recipe with ID {} not found during update attempt by admin.", recipeToEdit.getId());
            System.err.println(ANSI_RED + e.getMessage() + ANSI_RESET);
        } catch (Exception e) {
            log.error("Error updating recipe ID {}: {}", recipeToEdit.getId(), e.getMessage(), e);
            System.err.println(ANSI_RED + "Error updating recipe: " + e.getMessage() + ". Please check logs." + ANSI_RESET);
        }
    }

    /**
     * Handles deleting a specific, already selected recipe by an admin.
     * @param recipeToDelete The recipe object to be deleted.
     */
    private void deleteSelectedRecipe(Recipe recipeToDelete) {
        System.out.println(ANSI_YELLOW + "\nAre you sure you want to delete the recipe: '" + recipeToDelete.getTitle() + "' (ID: " + recipeToDelete.getId() + ")? (y/n): " + ANSI_RESET);
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y")) {
            try {
                recipeService.deleteRecipe(recipeToDelete.getId());
                log.info("Admin deleted recipe ID {}: '{}'", recipeToDelete.getId(), recipeToDelete.getTitle());
                System.out.println(ANSI_GREEN + "Recipe '" + recipeToDelete.getTitle() + "' deleted successfully!" + ANSI_RESET);
            } catch (RecipeNotFoundException e) {
                log.warn("Recipe with ID {} not found during delete attempt by admin (should not occur if recipe object was valid).", recipeToDelete.getId());
                System.err.println(ANSI_RED + e.getMessage() + ANSI_RESET);
            } catch (Exception e) {
                log.error("Error deleting recipe ID {}: {}", recipeToDelete.getId(), e.getMessage(), e);
                System.err.println(ANSI_RED + "Error deleting recipe: " + e.getMessage() + ". Please check logs." + ANSI_RESET);
            }
        } else {
            System.out.println(ANSI_YELLOW + "Deletion cancelled." + ANSI_RESET);
        }
    }

    /**
     * General method for admin to edit a recipe by providing its ID.
     * Guides user to find recipe first if ID is unknown.
     */
    private void editRecipe() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Edit Recipe (General) ---" + ANSI_RESET);
        System.out.println("To edit a specific recipe, please first find it using 'Manage Recipes' or 'Search Recipes'.");
        System.out.println("This general edit option requires you to know the recipe ID from any category.");
        System.out.print("Enter ID of the recipe to edit (or type 'cancel'): ");
        String idToEdit = scanner.nextLine().trim();
        if (idToEdit.equalsIgnoreCase("cancel") || idToEdit.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Edit cancelled." + ANSI_RESET);
            return;
        }
        try {
            Recipe recipeToEdit = recipeService.getRecipeById(idToEdit);
            editSelectedRecipe(recipeToEdit);
        } catch (RecipeNotFoundException e) {
            System.out.println(ANSI_RED + "Recipe with ID '" + idToEdit + "' not found across all categories." + ANSI_RESET);
        } catch (Exception e) {
            log.error("Error preparing to edit recipe ID {}: {}", idToEdit, e.getMessage(), e);
            System.err.println(ANSI_RED + "An error occurred: " + e.getMessage() + ANSI_RESET);
        }
    }

    /**
     * General method for admin to delete a recipe by providing its ID.
     * Guides user to find recipe first if ID is unknown.
     */
    private void deleteRecipe() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n--- Delete Recipe (General) ---" + ANSI_RESET);
        System.out.println("To delete a specific recipe, please first find it using 'Manage Recipes' or 'Search Recipes'.");
        System.out.println("This general delete option requires you to know the recipe ID from any category.");
        System.out.print("Enter ID of the recipe to delete (or type 'cancel'): ");
        String idToDelete = scanner.nextLine().trim();
        if (idToDelete.equalsIgnoreCase("cancel") || idToDelete.isEmpty()) {
            System.out.println(ANSI_YELLOW + "Delete cancelled." + ANSI_RESET);
            return;
        }
        try {
            Recipe recipeToDelete = recipeService.getRecipeById(idToDelete);
            deleteSelectedRecipe(recipeToDelete);
        } catch (RecipeNotFoundException e) {
            System.out.println(ANSI_RED + "Recipe with ID '" + idToDelete + "' not found across all categories." + ANSI_RESET);
        } catch (Exception e) {
            log.error("Error preparing to delete recipe ID {}: {}", idToDelete, e.getMessage(), e);
            System.err.println(ANSI_RED + "An error occurred: " + e.getMessage() + ANSI_RESET);
        }
    }
}