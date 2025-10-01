-- TODO: Add database schema definitions
// File: src/main/resources/schema.sql

-- SYOS-POS Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS syos_pos;
USE syos_pos;

-- User table for system users (Cashier, Manager, Admin)
CREATE TABLE user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('CASHIER', 'MANAGER', 'ADMIN', 'ONLINE_CUSTOMER') NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Normal (counter) customer table
CREATE TABLE customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Online customer table (separate from normal customers)
CREATE TABLE online_customer (
    online_customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Item table
CREATE TABLE item (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount DECIMAL(5,2) DEFAULT 0.00,
    reorder_level INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
);

-- Stock batch table
CREATE TABLE stock_batch (
    batch_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    quantity_received INT NOT NULL,
    quantity_remaining INT NOT NULL,
    purchase_date DATE NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    INDEX idx_item_date (item_id, purchase_date)
);

-- Shelf stock table (counter sales)
CREATE TABLE shelf_stock (
    shelf_stock_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE
);

-- Website inventory table (online sales)
CREATE TABLE website_inventory (
    web_inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE
);

-- Bill table
CREATE TABLE bill (
    bill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) UNIQUE NOT NULL,
    bill_date DATETIME NOT NULL,
    transaction_type ENUM('COUNTER', 'ONLINE') NOT NULL,
    customer_id BIGINT,
    online_customer_id BIGINT,
    customer_name VARCHAR(100),
    subtotal DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CREDIT_CARD', 'PAYPAL') NOT NULL,
    cash_tendered DECIMAL(10,2),
    change_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_serial (serial_number),
    INDEX idx_date (bill_date),
    INDEX idx_type (transaction_type)
);

-- Bill item table
CREATE TABLE bill_item (
    bill_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bill(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES item(item_id)
);

-- Sample data for testing
INSERT INTO user (username, password_hash, full_name, role) VALUES
('cashier', '$HASHED_PASSWORD', 'John Cashier', 'CASHIER'),
('manager', '$HASHED_PASSWORD', 'Jane Manager', 'MANAGER'),
('admin', '$HASHED_PASSWORD', 'Admin User', 'ADMIN');

INSERT INTO item (name, code, unit_price, discount, reorder_level) VALUES
('Rice 1kg', 'RICE001', 150.00, 5.00, 100),
('Sugar 1kg', 'SUGAR001', 120.00, 0.00, 80),
('Milk Powder 400g', 'MILK001', 850.00, 10.00, 50),
('Bread', 'BREAD001', 80.00, 0.00, 200),
('Eggs (10 pack)', 'EGG001', 350.00, 5.00, 50);

INSERT INTO shelf_stock (item_id, quantity)
SELECT item_id, 100 FROM item;

