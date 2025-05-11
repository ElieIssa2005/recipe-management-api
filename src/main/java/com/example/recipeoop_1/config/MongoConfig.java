package com.example.recipeoop_1.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${MONGODB_URI:mongodb+srv://elieissa:1234@cluster0.wgnomye.mongodb.net/recipe_db?retryWrites=true&w=majority&appName=Cluster0}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        // Explicitly return the database name
        return "recipe_db";
    }

    @Override
    @Bean
    @Primary
    public MongoClient mongoClient() {
        System.out.println("Creating MongoDB client with URI pattern: " +
                (mongoUri.startsWith("mongodb") ? "Valid URI pattern" : "Invalid URI pattern"));
        return MongoClients.create(mongoUri);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}