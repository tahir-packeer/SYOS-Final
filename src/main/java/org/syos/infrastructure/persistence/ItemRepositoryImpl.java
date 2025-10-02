// File: src/main/java/org/syos/infrastructure/persistence/ItemRepositoryImpl.java
package org.syos.infrastructure.persistence;

import org.syos.application.repository.ItemRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ItemRepository.
 * Follows Repository pattern and uses connection pooling.
 */
public class ItemRepositoryImpl implements ItemRepository {
    private final DBConnection dbConnection;
    
    public ItemRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }
    
    @Override
    public Item save(Item item) {
        String sql = "INSERT INTO item (name, code, unit_price, discount, reorder_level) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, 
                    Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, item.getName());
                stmt.setString(2, item.getCode().getCode());
                stmt.setBigDecimal(3, item.getUnitPrice().getAmount());
                stmt.setBigDecimal(4, item.getDiscount());
                stmt.setInt(5, item.getReorderLevel());
                
                stmt.executeUpdate();
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setItemId(generatedKeys.getLong(1));
                    }
                }
            }
            
            return item;
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error saving item", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Optional<Item> findById(Long itemId) {
        String sql = "SELECT * FROM item WHERE item_id = ?";
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, itemId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToItem(rs));
                    }
                }
            }
            
            return Optional.empty();
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding item by ID", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Optional<Item> findByCode(ItemCode code) {
        String sql = "SELECT * FROM item WHERE code = ?";
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, code.getCode());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToItem(rs));
                    }
                }
            }
            
            return Optional.empty();
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding item by code", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Item> findAll() {
        String sql = "SELECT * FROM item ORDER BY name";
        List<Item> items = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    items.add(mapResultSetToItem(rs));
                }
            }
            
            return items;
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all items", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<Item> searchByName(String name) {
        String sql = "SELECT * FROM item WHERE name LIKE ? ORDER BY name";
        List<Item> items = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + name + "%");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapResultSetToItem(rs));
                    }
                }
            }
            
            return items;
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error searching items by name", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public void update(Item item) {
        String sql = "UPDATE item SET name = ?, code = ?, unit_price = ?, " +
                    "discount = ?, reorder_level = ? WHERE item_id = ?";
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, item.getName());
                stmt.setString(2, item.getCode().getCode());
                stmt.setBigDecimal(3, item.getUnitPrice().getAmount());
                stmt.setBigDecimal(4, item.getDiscount());
                stmt.setInt(5, item.getReorderLevel());
                stmt.setLong(6, item.getItemId());
                
                stmt.executeUpdate();
            }
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error updating item", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public void delete(Long itemId) {
        String sql = "DELETE FROM item WHERE item_id = ?";
        Connection conn = null;
        
        try {
            conn = dbConnection.getConnection();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, itemId);
                stmt.executeUpdate();
            }
            
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error deleting item", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean existsByCode(ItemCode code) {
        return findByCode(code).isPresent();
    }
    
    /**
     * Helper method to map ResultSet to Item entity.
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        return new Item(
            rs.getLong("item_id"),
            rs.getString("name"),
            new ItemCode(rs.getString("code")),
            new Money(rs.getBigDecimal("unit_price")),
            rs.getBigDecimal("discount"),
            rs.getInt("reorder_level")
        );
    }
}
