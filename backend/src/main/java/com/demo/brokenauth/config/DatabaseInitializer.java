package com.demo.brokenauth.config;

import com.demo.brokenauth.model.User;
import com.demo.brokenauth.repository.UserRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final DatabaseClient databaseClient;
    
    public DatabaseInitializer(UserRepository userRepository, DatabaseClient databaseClient) {
        this.userRepository = userRepository;
        this.databaseClient = databaseClient;
    }
    
    @Override
    public void run(String... args) {
        // Create table if not exists
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                USERNAME VARCHAR(255) NOT NULL UNIQUE,
                PASSWORD VARCHAR(255) NOT NULL,
                EMAIL VARCHAR(255),
                ROLE VARCHAR(50)
            )
            """;
        
        databaseClient.sql(createTableSql)
            .fetch()
            .rowsUpdated()
            .doOnError(error -> System.err.println("Error creating table: " + error.getMessage()))
            .thenMany(
                // Initialize with sample users (passwords in plain text - vulnerability)
                Flux.just(
                    new User(null, "admin", "admin123", "admin@demo.com", "ADMIN"),
                    new User(null, "user1", "password123", "user1@demo.com", "USER"),
                    new User(null, "victim", "qwerty", "victim@demo.com", "USER")
                )
                .flatMap(userRepository::save)
                .doOnNext(user -> System.out.println("Created user: " + user.getUsername()))
                .doOnError(error -> System.err.println("Error creating users: " + error.getMessage()))
            )
            .subscribe(
                null,
                error -> System.err.println("Database initialization error: " + error.getMessage())
            );
    }
}
