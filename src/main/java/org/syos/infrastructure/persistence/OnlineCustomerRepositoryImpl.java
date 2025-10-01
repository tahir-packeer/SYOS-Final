package org.syos.infrastructure.persistence;

import org.syos.application.repository.OnlineCustomerRepository;
import org.syos.domain.entity.OnlineCustomer;
import org.syos.domain.valueobject.Password;
import org.syos.infrastructure.persistence.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of OnlineCustomerRepository.
 * Handles persistence of online customer entities to the database.
 */
public class OnlineCustomerRepositoryImpl implements OnlineCustomerRepository {

    private final DBConnection dbConnection;

    public OnlineCustomerRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public OnlineCustomer save(OnlineCustomer customer) {
        String sql = "INSERT INTO online_customer (name, email, address, password_hash) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPassword().getHashedValue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating online customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setOnlineCustomerId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating online customer failed, no ID obtained.");
                }
            }

            return customer;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving online customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<OnlineCustomer> findById(Long customerId) {
        String sql = "SELECT online_customer_id, name, email, address, password_hash FROM online_customer WHERE online_customer_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOnlineCustomer(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding online customer by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<OnlineCustomer> findByEmail(String email) {
        String sql = "SELECT online_customer_id, name, email, address, password_hash FROM online_customer WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOnlineCustomer(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding online customer by email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<OnlineCustomer> findAll() {
        String sql = "SELECT online_customer_id, name, email, address, password_hash FROM online_customer ORDER BY name";
        List<OnlineCustomer> customers = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapResultSetToOnlineCustomer(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all online customers: " + e.getMessage(), e);
        }

        return customers;
    }

    @Override
    public void update(OnlineCustomer customer) {
        String sql = "UPDATE online_customer SET name = ?, email = ?, address = ?, password_hash = ? WHERE online_customer_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPassword().getHashedValue());
            stmt.setLong(5, customer.getOnlineCustomerId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating online customer failed, no rows affected. Customer ID: "
                        + customer.getOnlineCustomerId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating online customer: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long customerId) {
        String sql = "DELETE FROM online_customer WHERE online_customer_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, customerId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting online customer failed, no rows affected. Customer ID: " + customerId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting online customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM online_customer WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error checking if online customer exists by email: " + e.getMessage(), e);
        }
    }

    private OnlineCustomer mapResultSetToOnlineCustomer(ResultSet rs) throws SQLException {
        Long id = rs.getLong("online_customer_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String address = rs.getString("address");
        String passwordHash = rs.getString("password_hash");

        Password password = new Password(passwordHash);

        return new OnlineCustomer(id, name, email, address, password);
    }
}
