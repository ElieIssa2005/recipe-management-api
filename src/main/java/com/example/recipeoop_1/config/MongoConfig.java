package com.example.recipeoop_1.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Configuration class for MongoDB connection and setup.
 * <p>
 * This class extends {@link AbstractMongoClientConfiguration} to provide
 * customized MongoDB client and template beans. It reads the MongoDB connection URI
 * from application properties and sets the default database name.
 * </p>
 *
 * @author Elie Issa/Michel Ghazaly
 * @version 1.0
 * @since 2025-05-14
 * @see AbstractMongoClientConfiguration
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    /**
     * The MongoDB connection URI.
     * This value is injected from the application properties ({@code MONGODB_URI}).
     * A default URI is provided if the property is not set.
     */
    @Value("${MONGODB_URI:mongodb+srv://elieissa:1234@cluster0.wgnomye.mongodb.net/recipe_db?retryWrites=true&w=majority&appName=Cluster0}")
    private String mongoUri;

    /**
     * Specifies the name of the MongoDB database to be used by the application.
     *
     * @return The database name, which is "recipe_db".
     */
    @Override
    protected String getDatabaseName() {
        // Explicitly return the database name
        return "recipe_db";
    }

    /**
     * Creates and configures the primary {@link MongoClient} bean.
     * <p>
     * This client is responsible for connecting to the MongoDB server specified by {@code mongoUri}.
     * It logs whether the URI pattern is valid before attempting to create the client.
     * </p>
     *
     * @return The configured {@link MongoClient} instance.
     * @see Primary
     */
    @Override
    @Bean
    @Primary
    public MongoClient mongoClient() {
        System.out.println("Creating MongoDB client with URI pattern: " +
                (mongoUri.startsWith("mongodb") ? "Valid URI pattern" : "Invalid URI pattern"));
        return MongoClients.create(mongoUri);
    }

    /**
     * Creates and configures the primary {@link MongoTemplate} bean.
     * <p>
     * The MongoTemplate provides a high-level abstraction for interacting with MongoDB,
     * simplifying database operations. It uses the configured {@link #mongoClient()}
     * and {@link #getDatabaseName()}.
     * </p>
     *
     * @return The configured {@link MongoTemplate} instance.
     * @throws Exception if an error occurs during the creation of the MongoTemplate,
     * for example, if the {@link #mongoClient()} cannot be created.
     * @see Primary
     */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}