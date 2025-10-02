package org.syos.infrastructure.persistence;

import org.syos.application.repository.StockBatchRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.entity.StockBatch;
import org.syos.domain.valueobject.ItemCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of StockBatchRepository.
 * Handles persistence of stock batch entities to the database.
 */
public class StockBatchRepositoryImpl implements StockBatchRepository {

    private final DBConnection dbConnection;
    private final ItemRepositoryImpl itemRepository;

    public StockBatchRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
    }

    @Override
    public StockBatch save(StockBatch batch) {
        String sql = "INSERT INTO stock_batch (item_id, quantity_received, quantity_remaining, purchase_date, expiry_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, batch.getItem().getItemId());
            stmt.setInt(2, batch.getQuantityReceived());
            stmt.setInt(3, batch.getQuantityRemaining());
            stmt.setDate(4, Date.valueOf(batch.getPurchaseDate()));
            stmt.setDate(5, batch.getExpiryDate() != null ? Date.valueOf(batch.getExpiryDate()) : null);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating stock batch failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    batch.setBatchId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating stock batch failed, no ID obtained.");
                }
            }

            return batch;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error saving stock batch: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<StockBatch> findById(Long batchId) {
        String sql = "SELECT sb.batch_id, sb.item_id, sb.quantity_received, sb.quantity_remaining, sb.purchase_date, sb.expiry_date "
                +
                "FROM stock_batch sb WHERE sb.batch_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, batchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStockBatch(rs));
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding stock batch by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockBatch> findByItemCode(ItemCode code) {
        String sql = "SELECT sb.batch_id, sb.item_id, sb.quantity_received, sb.quantity_remaining, sb.purchase_date, sb.expiry_date "
                +
                "FROM stock_batch sb JOIN item i ON sb.item_id = i.item_id WHERE i.code = ?";
        List<StockBatch> batches = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapResultSetToStockBatch(rs));
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding stock batches by item code: " + e.getMessage(), e);
        }

        return batches;
    }

    @Override
    public List<StockBatch> findByItemCodeOrderedByDate(ItemCode code) {
        String sql = "SELECT sb.batch_id, sb.item_id, sb.quantity_received, sb.quantity_remaining, sb.purchase_date, sb.expiry_date "
                +
                "FROM stock_batch sb JOIN item i ON sb.item_id = i.item_id WHERE i.code = ? ORDER BY sb.purchase_date ASC";
        List<StockBatch> batches = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code.getCode());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(mapResultSetToStockBatch(rs));
                }
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding stock batches by item code ordered by date: " + e.getMessage(),
                    e);
        }

        return batches;
    }

    @Override
    public List<StockBatch> findAll() {
        String sql = "SELECT sb.batch_id, sb.item_id, sb.quantity_received, sb.quantity_remaining, sb.purchase_date, sb.expiry_date "
                +
                "FROM stock_batch sb ORDER BY sb.purchase_date DESC";
        List<StockBatch> batches = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                batches.add(mapResultSetToStockBatch(rs));
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all stock batches: " + e.getMessage(), e);
        }

        return batches;
    }

    @Override
    public void update(StockBatch batch) {
        String sql = "UPDATE stock_batch SET item_id = ?, quantity_received = ?, quantity_remaining = ?, purchase_date = ?, expiry_date = ? WHERE batch_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, batch.getItem().getItemId());
            stmt.setInt(2, batch.getQuantityReceived());
            stmt.setInt(3, batch.getQuantityRemaining());
            stmt.setDate(4, Date.valueOf(batch.getPurchaseDate()));
            stmt.setDate(5, batch.getExpiryDate() != null ? Date.valueOf(batch.getExpiryDate()) : null);
            stmt.setLong(6, batch.getBatchId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException(
                        "Updating stock batch failed, no rows affected. Batch ID: " + batch.getBatchId());
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error updating stock batch: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long batchId) {
        String sql = "DELETE FROM stock_batch WHERE batch_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, batchId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting stock batch failed, no rows affected. Batch ID: " + batchId);
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error deleting stock batch: " + e.getMessage(), e);
        }
    }

    private StockBatch mapResultSetToStockBatch(ResultSet rs) throws SQLException {
        Long batchId = rs.getLong("batch_id");
        Long itemId = rs.getLong("item_id");
        int quantityReceived = rs.getInt("quantity_received");
        int quantityRemaining = rs.getInt("quantity_remaining");
        LocalDate purchaseDate = rs.getDate("purchase_date").toLocalDate();
        Date expiryDateSql = rs.getDate("expiry_date");
        LocalDate expiryDate = expiryDateSql != null ? expiryDateSql.toLocalDate() : null;

        // Get the item by item ID
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new SQLException("Item not found for ID: " + itemId);
        }

        return new StockBatch(batchId, itemOpt.get(), quantityReceived, quantityRemaining, purchaseDate, expiryDate);
    }
}
