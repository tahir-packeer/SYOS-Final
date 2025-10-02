package org.syos.infrastructure.persistence;

import org.syos.application.repository.WebsiteInventoryRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.entity.WebsiteInventory;
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
 * JDBC implementation of WebsiteInventoryRepository.
 * Handles persistence of website inventory entities to the database.
 */
public class WebsiteInventoryRepositoryImpl implements WebsiteInventoryRepository {

    private final DBConnection dbConnection;
    private final ItemRepositoryImpl itemRepository;

    public WebsiteInventoryRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
    }

    @Override
    public WebsiteInventory save(WebsiteInventory inventory) {
        String sql = "INSERT INTO website_inventory (item_id, quantity) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, inventory.getItem().getItemId());
            stmt.setInt(2, inventory.getQuantity());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating website inventory failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    inventory.setWebInventoryId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating website inventory failed, no ID obtained.");
                }
            }

            return inventory;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error saving website inventory: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<WebsiteInventory> findByItemCode(ItemCode code) {
        String sql = "SELECT wi.web_inventory_id, wi.item_id, wi.quantity FROM website_inventory wi WHERE wi.item_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToWebsiteInventory(rs));
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding website inventory by item code: " + e.getMessage(), e);
        }
    }

    @Override
    public List<WebsiteInventory> findAll() {
        String sql = "SELECT wi.web_inventory_id, wi.item_id, wi.quantity FROM website_inventory wi ORDER BY wi.item_id";
        List<WebsiteInventory> inventories = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                inventories.add(mapResultSetToWebsiteInventory(rs));
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all website inventory: " + e.getMessage(), e);
        }

        return inventories;
    }

    @Override
    public List<WebsiteInventory> findAvailableItems() {
        String sql = "SELECT wi.web_inventory_id, wi.item_id, wi.quantity FROM website_inventory wi WHERE wi.quantity > 0 ORDER BY wi.item_id";
        List<WebsiteInventory> inventories = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                inventories.add(mapResultSetToWebsiteInventory(rs));
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding available website inventory: " + e.getMessage(), e);
        }

        return inventories;
    }

    @Override
    public void update(WebsiteInventory inventory) {
        String sql = "UPDATE website_inventory SET item_id = ?, quantity = ? WHERE web_inventory_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inventory.getItem().getCode().getCode());
            stmt.setInt(2, inventory.getQuantity());
            stmt.setLong(3, inventory.getWebInventoryId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating website inventory failed, no rows affected. Web Inventory ID: "
                        + inventory.getWebInventoryId());
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error updating website inventory: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(ItemCode code) {
        String sql = "DELETE FROM website_inventory WHERE item_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException(
                        "Deleting website inventory failed, no rows affected. Item Code: " + code.getCode());
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error deleting website inventory: " + e.getMessage(), e);
        }
    }

    private WebsiteInventory mapResultSetToWebsiteInventory(ResultSet rs) throws SQLException {
        Long webInventoryId = rs.getLong("web_inventory_id");
        String itemCodeValue = rs.getString("item_id");
        int quantity = rs.getInt("quantity");

        // Get the item by item code
        ItemCode itemCode = new ItemCode(itemCodeValue);
        Optional<Item> itemOpt = itemRepository.findByCode(itemCode);
        if (!itemOpt.isPresent()) {
            throw new SQLException("Item not found for code: " + itemCodeValue);
        }

        return new WebsiteInventory(webInventoryId, itemOpt.get(), quantity);
    }
}
