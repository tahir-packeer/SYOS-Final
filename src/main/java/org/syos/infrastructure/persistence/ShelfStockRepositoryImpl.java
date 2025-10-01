package org.syos.infrastructure.persistence;

import org.syos.application.repository.ShelfStockRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.entity.ShelfStock;
import org.syos.domain.valueobject.ItemCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ShelfStockRepository.
 * Handles persistence of shelf stock entities to the database.
 */
public class ShelfStockRepositoryImpl implements ShelfStockRepository {

    private final DBConnection dbConnection;
    private final ItemRepositoryImpl itemRepository;

    public ShelfStockRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
    }

    @Override
    public ShelfStock save(ShelfStock stock) {
        String sql = "INSERT INTO shelf_stock (item_id, quantity) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, stock.getItem().getItemId());
            stmt.setInt(2, stock.getQuantity());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating shelf stock failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    stock.setShelfStockId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating shelf stock failed, no ID obtained.");
                }
            }

            return stock;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving shelf stock: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ShelfStock> findByItemCode(ItemCode code) {
        String sql = "SELECT ss.shelf_stock_id, ss.item_id, ss.quantity FROM shelf_stock ss " +
                "JOIN item i ON ss.item_id = i.item_id WHERE i.code = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToShelfStock(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding shelf stock by item code: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ShelfStock> findAll() {
        String sql = "SELECT ss.shelf_stock_id, ss.item_id, ss.quantity FROM shelf_stock ss ORDER BY ss.item_id";
        List<ShelfStock> stocks = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stocks.add(mapResultSetToShelfStock(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all shelf stock: " + e.getMessage(), e);
        }

        return stocks;
    }

    @Override
    public List<ShelfStock> findBelowReorderLevel() {
        String sql = "SELECT ss.shelf_stock_id, ss.item_id, ss.quantity, i.reorder_level " +
                "FROM shelf_stock ss " +
                "JOIN item i ON ss.item_id = i.item_id " +
                "WHERE ss.quantity < i.reorder_level " +
                "ORDER BY ss.item_id";
        List<ShelfStock> stocks = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stocks.add(mapResultSetToShelfStock(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding shelf stock below reorder level: " + e.getMessage(), e);
        }

        return stocks;
    }

    @Override
    public void update(ShelfStock stock) {
        String sql = "UPDATE shelf_stock SET item_id = ?, quantity = ? WHERE shelf_stock_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, stock.getItem().getItemId());
            stmt.setInt(2, stock.getQuantity());
            stmt.setLong(3, stock.getShelfStockId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException(
                        "Updating shelf stock failed, no rows affected. Shelf Stock ID: " + stock.getShelfStockId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating shelf stock: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(ItemCode code) {
        String sql = "DELETE FROM shelf_stock WHERE item_id = (SELECT item_id FROM item WHERE code = ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting shelf stock failed, no rows affected. Item Code: " + code.getCode());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting shelf stock: " + e.getMessage(), e);
        }
    }

    private ShelfStock mapResultSetToShelfStock(ResultSet rs) throws SQLException {
        Long shelfStockId = rs.getLong("shelf_stock_id");
        Long itemId = rs.getLong("item_id");
        int quantity = rs.getInt("quantity");

        // Get the item by item ID
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new SQLException("Item not found for ID: " + itemId);
        }

        return new ShelfStock(shelfStockId, itemOpt.get(), quantity);
    }
}
