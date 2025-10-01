# SYOS POS System - Database Setup

## Quick Setup for Your MySQL Database

### 1. Update Database Configuration

Update your database connection details in `src/main/resources/application.properties`:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/your_database_name
db.username=your_username
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
db.pool.size=5
```

### 2. Create Database Tables

Run the SQL script `schema_only.sql` in your MySQL database to create all required tables:

```sql
-- This creates all necessary tables without any hardcoded data
-- Tables: users, customers, online_customers, items, stock_batches, shelf_stock, website_inventory, bills, bill_items
```

### 3. Create Your First User

Use the CreateUser utility to add users to your database:

```bash
mvn compile
mvn exec:java -Dexec.mainClass=org.syos.infrastructure.util.CreateUser
```

This will prompt you to create users with roles:

- **ADMIN** - Full access to all features
- **MANAGER** - Inventory and report management
- **CASHIER** - Point of sale operations

### 4. Run the Application

```bash
mvn exec:java
```

## No Hardcoded Data

- ✅ No test users or sample data are automatically created
- ✅ Clean database tables ready for your data
- ✅ You control what data goes into your database
- ✅ Use the CreateUser utility to add users as needed

## Adding Your Own Data

You can add your own:

- **Items**: Through the Manager interface after logging in
- **Customers**: Added automatically during sales or through Manager interface
- **Stock**: Managed through the inventory features
- **Users**: Use the CreateUser utility or add directly to database

## Database Schema

The system creates these tables:

- `users` - System users (admin, manager, cashier)
- `customers` - Walk-in customers
- `online_customers` - Website customers
- `items` - Products/inventory items
- `stock_batches` - Inventory batches with expiry tracking
- `shelf_stock` - Counter sales inventory
- `website_inventory` - Online sales inventory
- `bills` - Sales transactions
- `bill_items` - Individual items in each bill

All tables use proper foreign key relationships and indexing for performance.
