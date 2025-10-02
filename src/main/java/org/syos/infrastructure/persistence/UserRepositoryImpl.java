package org.syos.infrastructure.persistence;

import org.syos.application.repository.UserRepository;
import org.syos.domain.entity.User;
import org.syos.domain.enums.UserRole;
import org.syos.domain.valueobject.Password;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserRepository.
 * Follows Repository pattern and uses connection pooling.
 */
public class UserRepositoryImpl implements UserRepository {
    private final DBConnection dbConnection;

    public UserRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO user (username, password_hash, full_name, role, active) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword().getHashedValue());
                stmt.setString(3, user.getFullName());
                stmt.setString(4, user.getRole().name());
                stmt.setBoolean(5, user.isActive());

                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getLong(1));
                    }
                }
            }

            return user;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error saving user", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding user by ID", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM user ORDER BY username";
        List<User> users = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }

            return users;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM user WHERE role = ? ORDER BY username";
        List<User> users = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, role.name());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
            }

            return users;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding users by role", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE user SET username = ?, password_hash = ?, full_name = ?, " +
                "role = ?, active = ? WHERE user_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword().getHashedValue());
                stmt.setString(3, user.getFullName());
                stmt.setString(4, user.getRole().name());
                stmt.setBoolean(5, user.isActive());
                stmt.setLong(6, user.getUserId());

                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error updating user", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public void delete(Long userId) {
        String sql = "DELETE FROM user WHERE user_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                stmt.executeUpdate();
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    /**
     * Helper method to map ResultSet to User entity.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("username"),
                new Password(rs.getString("password_hash")),
                rs.getString("full_name"),
                UserRole.valueOf(rs.getString("role")),
                rs.getBoolean("active"));
    }
}
