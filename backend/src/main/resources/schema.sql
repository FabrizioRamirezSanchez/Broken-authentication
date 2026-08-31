-- Create users table for the vulnerable authentication demo
-- VULNERABILITY: No constraints on password strength, email format, etc.
-- FIX: Add proper constraints and validation

CREATE TABLE IF NOT EXISTS users (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(255) NOT NULL UNIQUE,
    PASSWORD VARCHAR(255) NOT NULL,
    EMAIL VARCHAR(255),
    ROLE VARCHAR(50)
);
