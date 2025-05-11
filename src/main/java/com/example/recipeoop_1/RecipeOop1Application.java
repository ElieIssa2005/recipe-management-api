package com.example.recipeoop_1;

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

@SpringBootApplication
@EnableMongoRepositories
public class RecipeOop1Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RecipeOop1Application.class, args);

        // Check if running in production profile
        Environment env = context.getEnvironment();
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");

        // Only run console UI if not in production mode
        if (!isProd) {
            ConsoleUI consoleUI = context.getBean(ConsoleUI.class);
            consoleUI.start();
            // Don't close the context in prod since we need the web server to stay running
            context.close();
        }
    }
}

@Component
class ConsoleUI implements CommandLineRunner {
    private final RecipeService recipeService;
    private final CategoryService categoryService;
    private final Scanner scanner;

    // Admin password
    private static final String ADMIN_PASSWORD = "1234";

    // Constructor injection of services
    public ConsoleUI(RecipeService recipeService, CategoryService categoryService) {
        this.recipeService = recipeService;
        this.categoryService = categoryService;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run(String... args) {
        // This method is called automatically by Spring Boot
        // We'll start the UI in the start() method instead
    }

    /**
     * Start the console UI
     */
    public void start() {
        System.out.println("Welcome to Recipe Management System!");

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

        scanner.close();
    }

    /**
     * Client menu - browse recipes by category
     */
    private void clientMenu() {
        boolean backToMain = false;

        while (!backToMain) {
            // Get all available categories
            List<String> categories = categoryService.getAllCategories();

            if (categories.isEmpty()) {
                System.out.println("No recipes found in the database.");
                return;
            }

            // Display categories
            System.out.println("\nAvailable Categories:");
            for (int i = 0; i < categories.size(); i++) {
                System.out.println((i + 1) + ". " + categories.get(i).replace("recipe_", ""));
            }
            System.out.println((categories.size() + 1) + ". Search for recipes");
            System.out.println((categories.size() + 2) + ". Back to main menu");

            // Get user choice
            System.out.print("Select a category or option: ");
            String choice = scanner.nextLine();

            try {
                int categoryIndex = Integer.parseInt(choice) - 1;

                if (categoryIndex >= 0 && categoryIndex < categories.size()) {
                    // Show recipes in selected category
                    String selectedCategory = categories.get(categoryIndex).replace("recipe_", "");
                    showRecipesByCategory(selectedCategory);
                } else if (categoryIndex == categories.size()) {
                    // Search for recipes
                    searchRecipesMenu();
                } else if (categoryIndex == categories.size() + 1) {
                    // Back to main menu
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
     * Search recipes menu
     */
    private void searchRecipesMenu() {
        boolean backToClientMenu = false;

        while (!backToClientMenu) {
            System.out.println("\nSearch Options:");
            System.out.println("1. Search by title");
            System.out.println("2. Search by cooking time");
            System.out.println("3. Search by ingredient");
            System.out.println("4. Advanced search");
            System.out.println("5. Back to category menu");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();
            List<Recipe> searchResults = new ArrayList<>();

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
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }
                    break;
                case "3":
                    System.out.print("Enter ingredient keyword: ");
                    String ingredient = scanner.nextLine();
                    searchResults = recipeService.searchRecipesByIngredient(ingredient);
                    break;
                case "4":
                    advancedSearchMenu();
                    continue;
                case "5":
                    backToClientMenu = true;
                    continue;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    continue;
            }

            // Display search results
            if (searchResults.isEmpty()) {
                System.out.println("No recipes found matching your search criteria.");
            } else {
                displayRecipeList("Search Results", searchResults);
            }
        }
    }

    /**
     * Advanced search menu
     */
    private void advancedSearchMenu() {
        System.out.println("\nAdvanced Search (leave blank to skip):");

        System.out.print("Title contains: ");
        String title = scanner.nextLine();
        title = title.isEmpty() ? null : title;

        System.out.print("Category: ");
        String category = scanner.nextLine();
        category = category.isEmpty() ? null : category;

        System.out.print("Maximum cooking time (minutes): ");
        String cookingTimeStr = scanner.nextLine();
        Integer cookingTime = null;
        if (!cookingTimeStr.isEmpty()) {
            try {
                cookingTime = Integer.parseInt(cookingTimeStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid cooking time. Ignoring this criterion.");
            }
        }

        System.out.print("Ingredient contains: ");
        String ingredient = scanner.nextLine();
        ingredient = ingredient.isEmpty() ? null : ingredient;

        // Perform search
        List<Recipe> searchResults = recipeService.advancedSearch(title, category, cookingTime, ingredient);

        // Display results
        if (searchResults.isEmpty()) {
            System.out.println("No recipes found matching your search criteria.");
        } else {
            displayRecipeList("Advanced Search Results", searchResults);
        }
    }

    /**
     * Display a list of recipes and allow user to select one to view
     */
    private void displayRecipeList(String title, List<Recipe> recipes) {
        boolean backToMenu = false;

        while (!backToMenu) {
            System.out.println("\n" + title + ":");
            for (int i = 0; i < recipes.size(); i++) {
                Recipe recipe = recipes.get(i);
                System.out.println((i + 1) + ". " + recipe.getTitle() + " (Category: " + recipe.getCategory() +
                        ", Cooking time: " + recipe.getCookingTime() + " min)");
            }
            System.out.println((recipes.size() + 1) + ". Back");

            System.out.print("Select a recipe to view details: ");
            String choice = scanner.nextLine();

            try {
                int recipeIndex = Integer.parseInt(choice) - 1;

                if (recipeIndex >= 0 && recipeIndex < recipes.size()) {
                    showRecipeDetails(recipes.get(recipeIndex));
                } else if (recipeIndex == recipes.size()) {
                    backToMenu = true;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    /**
     * Show recipes by category
     */
    private void showRecipesByCategory(String category) {
        // Get recipes in category
        List<Recipe> recipes = recipeService.searchRecipesByCategory(category);

        if (recipes.isEmpty()) {
            System.out.println("No recipes found in category: " + category);
            return;
        }

        displayRecipeList("Recipes in category: " + category, recipes);
    }

    /**
     * Show recipe details
     */
    private void showRecipeDetails(Recipe recipe) {
        System.out.println("\n=== " + recipe.getTitle() + " ===");
        System.out.println("Category: " + recipe.getCategory());
        System.out.println("Cooking Time: " + recipe.getCookingTime() + " minutes");
        System.out.println("Created By: " + recipe.getCreatedBy());

        System.out.println("\nIngredients:");
        for (String ingredient : recipe.getIngredients()) {
            System.out.println("- " + ingredient);
        }

        System.out.println("\nInstructions:");
        System.out.println(recipe.getInstructions());

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Admin authentication
     */
    private void adminAuthentication() {
        System.out.println("\nAdmin Authentication");

        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter password (or 'exit' to cancel): ");
            String password = scanner.nextLine();

            if (password.equalsIgnoreCase("exit")) {
                return;
            }

            if (ADMIN_PASSWORD.equals(password)) {
                adminMenu();
                return;
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
     * Admin menu
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
            System.out.println("5. Search Recipes");
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
                    searchRecipesMenu();
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
     * View all recipes (admin)
     */
    private void viewAllRecipes() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();

        if (allRecipes.isEmpty()) {
            System.out.println("No recipes found in the database.");
            return;
        }

        displayRecipeList("All Recipes", allRecipes);
    }

    /**
     * Add new recipe (admin)
     */
    private void addNewRecipe() {
        System.out.println("\nAdd New Recipe:");

        Recipe newRecipe = new Recipe();

        System.out.print("Title: ");
        String title = scanner.nextLine();
        newRecipe.setTitle(title);

        System.out.print("Category: ");
        String category = scanner.nextLine();
        newRecipe.setCategory(category);

        System.out.print("Cooking Time (minutes): ");
        try {
            int cookingTime = Integer.parseInt(scanner.nextLine());
            newRecipe.setCookingTime(cookingTime);
        } catch (NumberFormatException e) {
            System.out.println("Invalid cooking time. Setting to 0.");
            newRecipe.setCookingTime(0);
        }

        System.out.println("Ingredients (enter empty line to finish):");
        List<String> ingredients = new ArrayList<>();
        while (true) {
            System.out.print("- ");
            String ingredient = scanner.nextLine();
            if (ingredient.trim().isEmpty()) {
                break;
            }
            ingredients.add(ingredient);
        }
        newRecipe.setIngredients(ingredients);

        System.out.println("Instructions:");
        String instructions = scanner.nextLine();
        newRecipe.setInstructions(instructions);

        // Save recipe
        Recipe saved = recipeService.createRecipe(newRecipe, "admin");

        System.out.println("Recipe saved successfully! ID: " + saved.getId());
    }

    /**
     * Edit recipe (admin)
     */
    private void editRecipe() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();

        if (allRecipes.isEmpty()) {
            System.out.println("No recipes found in the database.");
            return;
        }

        displayRecipeList("Select a recipe to edit", allRecipes);

        System.out.print("Enter the number of the recipe to edit (or 0 to cancel): ");
        String choice = scanner.nextLine();

        try {
            int recipeIndex = Integer.parseInt(choice) - 1;

            if (recipeIndex >= 0 && recipeIndex < allRecipes.size()) {
                Recipe recipeToEdit = allRecipes.get(recipeIndex);

                System.out.println("\nEditing Recipe: " + recipeToEdit.getTitle());
                System.out.println("(Press Enter to keep current value)");

                System.out.print("Title [" + recipeToEdit.getTitle() + "]: ");
                String title = scanner.nextLine();
                if (!title.trim().isEmpty()) {
                    recipeToEdit.setTitle(title);
                }

                System.out.print("Category [" + recipeToEdit.getCategory() + "]: ");
                String category = scanner.nextLine();
                if (!category.trim().isEmpty()) {
                    recipeToEdit.setCategory(category);
                }

                System.out.print("Cooking Time [" + recipeToEdit.getCookingTime() + "]: ");
                String cookingTimeStr = scanner.nextLine();
                if (!cookingTimeStr.trim().isEmpty()) {
                    try {
                        int cookingTime = Integer.parseInt(cookingTimeStr);
                        recipeToEdit.setCookingTime(cookingTime);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid cooking time. Keeping current value.");
                    }
                }

                System.out.println("Ingredients (Current: " + String.join(", ", recipeToEdit.getIngredients()) + ")");
                System.out.println("Do you want to modify ingredients? (y/n): ");
                String modifyIngredients = scanner.nextLine();

                if (modifyIngredients.equalsIgnoreCase("y")) {
                    System.out.println("Enter new ingredients (empty line to finish):");
                    List<String> ingredients = new ArrayList<>();
                    while (true) {
                        System.out.print("- ");
                        String ingredient = scanner.nextLine();
                        if (ingredient.trim().isEmpty()) {
                            break;
                        }
                        ingredients.add(ingredient);
                    }
                    if (!ingredients.isEmpty()) {
                        recipeToEdit.setIngredients(ingredients);
                    }
                }

                System.out.println("Current Instructions: " + recipeToEdit.getInstructions());
                System.out.print("New Instructions (press Enter to keep current): ");
                String instructions = scanner.nextLine();
                if (!instructions.trim().isEmpty()) {
                    recipeToEdit.setInstructions(instructions);
                }

                // Update recipe
                Recipe updated = recipeService.updateRecipe(recipeToEdit.getId(), recipeToEdit);

                System.out.println("Recipe updated successfully!");
            } else if (recipeIndex != -1) {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    /**
     * Delete recipe (admin)
     */
    private void deleteRecipe() {
        List<Recipe> allRecipes = recipeService.getAllRecipes();

        if (allRecipes.isEmpty()) {
            System.out.println("No recipes found in the database.");
            return;
        }

        displayRecipeList("Select a recipe to delete", allRecipes);

        System.out.print("Enter the number of the recipe to delete (or 0 to cancel): ");
        String choice = scanner.nextLine();

        try {
            int recipeIndex = Integer.parseInt(choice) - 1;

            if (recipeIndex >= 0 && recipeIndex < allRecipes.size()) {
                Recipe recipeToDelete = allRecipes.get(recipeIndex);

                System.out.println("\nAre you sure you want to delete the recipe '" + recipeToDelete.getTitle() + "'? (y/n): ");
                String confirm = scanner.nextLine();

                if (confirm.equalsIgnoreCase("y")) {
                    recipeService.deleteRecipe(recipeToDelete.getId());
                    System.out.println("Recipe deleted successfully!");
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else if (recipeIndex != -1) {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
}