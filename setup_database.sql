-- Setup Database for Orchid Management System

-- Create database if not exists
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'Orchid')
BEGIN
    CREATE DATABASE Orchid;
END
GO

USE Orchid;
GO

-- Create roles table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'roles')
BEGIN
    CREATE TABLE roles (
        role_id BIGINT PRIMARY KEY IDENTITY(1,1),
        role_name VARCHAR(50) NOT NULL UNIQUE
    );
END
GO

-- Create accounts table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'accounts')
BEGIN
    CREATE TABLE accounts (
        account_id BIGINT PRIMARY KEY IDENTITY(1,1),
        email VARCHAR(100) NOT NULL UNIQUE,
        account_name VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(100) NOT NULL,
        role_id BIGINT,
        is_active BIT NOT NULL DEFAULT 1,
        FOREIGN KEY (role_id) REFERENCES roles(role_id)
    );
END
GO

-- Create categories table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'categories')
BEGIN
    CREATE TABLE categories (
        category_id BIGINT PRIMARY KEY IDENTITY(1,1),
        category_name VARCHAR(50) NOT NULL UNIQUE
    );
END
GO

-- Create orchids table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orchids')
BEGIN
    CREATE TABLE orchids (
        orchid_id BIGINT PRIMARY KEY IDENTITY(1,1),
        orchid_name VARCHAR(255),
        orchid_description TEXT,
        orchid_url VARCHAR(500),
        price DECIMAL(10,2),
        is_natural BIT,
        category_id BIGINT NOT NULL,
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
    );
END
GO

-- Create orders table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
BEGIN
    CREATE TABLE orders (
        id BIGINT PRIMARY KEY IDENTITY(1,1),
        account_id BIGINT NOT NULL,
        order_date DATETIME,
        order_status VARCHAR(20),
        total_amount DECIMAL(10,2),
        FOREIGN KEY (account_id) REFERENCES accounts(account_id)
    );
END
GO

-- Create order_details table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'order_details')
BEGIN
    CREATE TABLE order_details (
        id BIGINT PRIMARY KEY IDENTITY(1,1),
        orchid_id BIGINT NOT NULL,
        price DECIMAL(10,2),
        quantity INT,
        order_id BIGINT NOT NULL,
        FOREIGN KEY (orchid_id) REFERENCES orchids(orchid_id),
        FOREIGN KEY (order_id) REFERENCES orders(id)
    );
END
GO

-- Insert default roles
IF NOT EXISTS (SELECT * FROM roles WHERE role_name = 'USER')
BEGIN
    INSERT INTO roles (role_name) VALUES ('USER');
END

IF NOT EXISTS (SELECT * FROM roles WHERE role_name = 'ADMIN')
BEGIN
    INSERT INTO roles (role_name) VALUES ('ADMIN');
END

IF NOT EXISTS (SELECT * FROM roles WHERE role_name = 'MODERATOR')
BEGIN
    INSERT INTO roles (role_name) VALUES ('MODERATOR');
END
GO

-- Insert test categories
IF NOT EXISTS (SELECT * FROM categories WHERE category_name = 'Phalaenopsis')
BEGIN
    INSERT INTO categories (category_name) VALUES ('Phalaenopsis');
END

IF NOT EXISTS (SELECT * FROM categories WHERE category_name = 'Cattleya')
BEGIN
    INSERT INTO categories (category_name) VALUES ('Cattleya');
END

IF NOT EXISTS (SELECT * FROM categories WHERE category_name = 'Dendrobium')
BEGIN
    INSERT INTO categories (category_name) VALUES ('Dendrobium');
END
GO

-- Insert test admin user (password: admin123)
-- Note: This password should be BCrypt encoded in real application
IF NOT EXISTS (SELECT * FROM accounts WHERE email = 'admin@example.com')
BEGIN
    INSERT INTO accounts (email, account_name, password, role_id, is_active)
    VALUES ('admin@example.com', 'Admin User', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 
            (SELECT role_id FROM roles WHERE role_name = 'ADMIN'), 1);
END
GO

-- Insert test user (password: user123)
IF NOT EXISTS (SELECT * FROM accounts WHERE email = 'user@example.com')
BEGIN
    INSERT INTO accounts (email, account_name, password, role_id, is_active)
    VALUES ('user@example.com', 'Test User', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 
            (SELECT role_id FROM roles WHERE role_name = 'USER'), 1);
END
GO

-- Insert test orchids
IF NOT EXISTS (SELECT * FROM orchids WHERE orchid_name = 'White Phalaenopsis')
BEGIN
    INSERT INTO orchids (orchid_name, orchid_description, orchid_url, price, is_natural, category_id)
    VALUES ('White Phalaenopsis', 'Beautiful white phalaenopsis orchid', 'https://example.com/white-phal.jpg', 25.99, 1,
            (SELECT category_id FROM categories WHERE category_name = 'Phalaenopsis'));
END

IF NOT EXISTS (SELECT * FROM orchids WHERE orchid_name = 'Purple Cattleya')
BEGIN
    INSERT INTO orchids (orchid_name, orchid_description, orchid_url, price, is_natural, category_id)
    VALUES ('Purple Cattleya', 'Stunning purple cattleya orchid', 'https://example.com/purple-cattleya.jpg', 35.99, 1,
            (SELECT category_id FROM categories WHERE category_name = 'Cattleya'));
END
GO

-- Display test data
SELECT 'Roles:' as info;
SELECT * FROM roles;

SELECT 'Accounts:' as info;
SELECT a.account_id, a.email, a.account_name, a.is_active, r.role_name 
FROM accounts a 
LEFT JOIN roles r ON a.role_id = r.role_id;

SELECT 'Categories:' as info;
SELECT * FROM categories;

SELECT 'Orchids:' as info;
SELECT o.orchid_id, o.orchid_name, o.price, o.is_natural, c.category_name
FROM orchids o
LEFT JOIN categories c ON o.category_id = c.category_id;
GO 