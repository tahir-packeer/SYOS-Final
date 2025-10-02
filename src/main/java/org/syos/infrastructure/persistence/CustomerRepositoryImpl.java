package org.syos.infrastructure.persistence;

import org.syos.application.repository.CustomerRepository;
import org.syos.domain.entity.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CustomerRepository.
 * Follows Repository pattern and uses connection pooling.
 */
public class CustomerRepositoryImpl implements CustomerRepository {
    private final DBConnection dbConnection;

    public CustomerRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Customer save(Customer customer) {
        String sql = "INSERT INTO customer (name, phone) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, customer.getName());
                stmt.setString(2, customer.getPhone());

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getLong(1));
                    }
                }
            }

            return customer;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error saving customer", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public Optional<Customer> findById(Long customerId) {
        String sql = "SELECT * FROM customer WHERE customer_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, customerId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCustomer(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding customer by ID", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM customer WHERE phone = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToCustomer(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding customer by phone", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customer ORDER BY name";
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs));
                }
            }

            return customers;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all customers", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Customer> searchByName(String name) {
        String sql = "SELECT * FROM customer WHERE name LIKE ? ORDER BY name";
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + name + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        customers.add(mapResultSetToCustomer(rs));
                    }
                }
            }

            return customers;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error searching customers by name", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customer SET name = ?, phone = ? WHERE customer_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, customer.getName());
                stmt.setString(2, customer.getPhone());
                stmt.setLong(3, customer.getCustomerId());

                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error updating customer", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public void delete(Long customerId) {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, customerId);
                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error deleting customer", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    /**
     * Helper method to map ResultSet to Customer entity.
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("customer_id"),
                rs.getString("name"),
                rs.getString("phone"));
    }
}
