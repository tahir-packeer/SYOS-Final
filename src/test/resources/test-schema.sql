-- Test Database Schema for H2
-- This mirrors the main schema but optimized for testing

-- Users table
CREATE TABLE users
(
    user_id BIGINT
    AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR
    (50) UNIQUE NOT NULL,
    password_hash VARCHAR
    (255) NOT NULL,
    full_name VARCHAR
    (100) NOT NULL,
    role VARCHAR
    (20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

    -- Customers table
    CREATE TABLE customers
    (
        customer_id BIGINT
        AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
        (100) NOT NULL,
    phone VARCHAR
        (15) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

        -- Online customer table
        CREATE TABLE online_customer
        (
            online_customer_id BIGINT
            AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
            (100) NOT NULL,
    email VARCHAR
            (100) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    password_hash VARCHAR
            (255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

            -- Item table
            CREATE TABLE item
            (
                item_id BIGINT
                AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR
                (100) NOT NULL,
    code VARCHAR
                (50) UNIQUE NOT NULL,
    unit_price DECIMAL
                (10,2) NOT NULL,
    discount DECIMAL
                (5,2) DEFAULT 0.00,
    reorder_level INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

                -- Stock batch table
                CREATE TABLE stock_batch
                (
                    batch_id BIGINT
                    AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    quantity_received INT NOT NULL,
    quantity_remaining INT NOT NULL,
    purchase_date DATE NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
                    (item_id) REFERENCES item
                    (item_id) ON
                    DELETE CASCADE
);

                    -- Shelf stock table
                    CREATE TABLE shelf_stock
                    (
                        shelf_stock_id BIGINT
                        AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
                        (item_id) REFERENCES item
                        (item_id) ON
                        DELETE CASCADE
);

                        -- Website inventory table
                        CREATE TABLE website_inventory
                        (
                            web_inventory_id BIGINT
                            AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY
                            (item_id) REFERENCES item
                            (item_id) ON
                            DELETE CASCADE
);

                            -- Bills table
                            CREATE TABLE bills
                            (
                                bill_id BIGINT
                                AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR
                                (50) UNIQUE NOT NULL,
    date_time TIMESTAMP NOT NULL,
    transaction_type VARCHAR
                                (20) NOT NULL,
    customer_id BIGINT,
    customer_name VARCHAR
                                (100) NOT NULL,
    subtotal DECIMAL
                                (10,2) NOT NULL,
    discount DECIMAL
                                (10,2) DEFAULT 0.00,
    total_amount DECIMAL
                                (10,2) NOT NULL,
    payment_method VARCHAR
                                (20) NOT NULL,
    cash_tendered DECIMAL
                                (10,2),
    change_amount DECIMAL
                                (10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

                                -- Bill items table
                                CREATE TABLE bill_items
                                (
                                    bill_item_id BIGINT
                                    AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL
                                    (10,2) NOT NULL,
    total_price DECIMAL
                                    (10,2) NOT NULL,
    FOREIGN KEY
                                    (bill_id) REFERENCES bills
                                    (bill_id) ON
                                    DELETE CASCADE,
    FOREIGN KEY (item_id)
                                    REFERENCES item
                                    (item_id) ON
                                    DELETE CASCADE
);