package org.syos.infrastructure.persistence;

import org.syos.application.repository.BillRepository;
import org.syos.domain.entity.Bill;
import org.syos.domain.entity.BillItem;
import org.syos.domain.enums.PaymentMethod;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of BillRepository.
 * Follows Repository pattern and uses connection pooling.
 */
public class BillRepositoryImpl implements BillRepository {
    private final DBConnection dbConnection;

    public BillRepositoryImpl(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Bill save(Bill bill) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert bill
            String billSql = "INSERT INTO bill (serial_number, bill_date, transaction_type, " +
                    "customer_id, customer_name, subtotal, discount, total_amount, " +
                    "payment_method, cash_tendered, change_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            long billId;
            try (PreparedStatement billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                billStmt.setString(1, bill.getSerialNumber());
                billStmt.setTimestamp(2, Timestamp.valueOf(bill.getDateTime()));
                billStmt.setString(3, bill.getTransactionType().name());
                billStmt.setObject(4, bill.getCustomerId());
                billStmt.setString(5, bill.getCustomerName());
                billStmt.setBigDecimal(6, bill.getSubtotal().getAmount());
                billStmt.setBigDecimal(7, bill.getDiscount().getAmount());
                billStmt.setBigDecimal(8, bill.getTotalAmount().getAmount());
                billStmt.setString(9, bill.getPaymentMethod().name());
                billStmt.setBigDecimal(10, bill.getCashTendered() != null ? bill.getCashTendered().getAmount() : null);
                billStmt.setBigDecimal(11, bill.getChangeAmount() != null ? bill.getChangeAmount().getAmount() : null);

                billStmt.executeUpdate();

                try (ResultSet generatedKeys = billStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        billId = generatedKeys.getLong(1);
                        bill.setBillId(billId);
                    } else {
                        throw new SQLException("Failed to get bill ID");
                    }
                }
            }

            // Insert bill items
            String itemSql = "INSERT INTO bill_item (bill_id, item_id, quantity, unit_price, total_price) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                for (BillItem item : bill.getItems()) {
                    itemStmt.setLong(1, billId);
                    itemStmt.setLong(2, item.getItem().getItemId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setBigDecimal(4, item.getUnitPrice().getAmount());
                    itemStmt.setBigDecimal(5, item.getTotalPrice().getAmount());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit();
            return bill;

        } catch (ClassNotFoundException | SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException("Error saving bill", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
                dbConnection.closeConnection(conn);
            }
        }
    }

    @Override
    public Optional<Bill> findById(Long billId) {
        String sql = "SELECT * FROM bill WHERE bill_id = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, billId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBill(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding bill by ID", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public Optional<Bill> findBySerialNumber(String serialNumber) {
        String sql = "SELECT * FROM bill WHERE serial_number = ?";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, serialNumber);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToBill(rs));
                    }
                }
            }

            return Optional.empty();

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding bill by serial number", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Bill> findAll() {
        String sql = "SELECT * FROM bill ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    bills.add(mapResultSetToBill(rs));
                }
            }

            return bills;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding all bills", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Bill> findByDate(LocalDate date) {
        String sql = "SELECT * FROM bill WHERE DATE(bill_date) = ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(mapResultSetToBill(rs));
                    }
                }
            }

            return bills;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding bills by date", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Bill> findByTransactionType(TransactionType type) {
        String sql = "SELECT * FROM bill WHERE transaction_type = ? ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, type.name());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(mapResultSetToBill(rs));
                    }
                }
            }

            return bills;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding bills by transaction type", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public List<Bill> findByDateAndType(LocalDate date, TransactionType type) {
        String sql = "SELECT * FROM bill WHERE DATE(bill_date) = ? AND transaction_type = ? " +
                "ORDER BY bill_date DESC";
        List<Bill> bills = new ArrayList<>();
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(date));
                stmt.setString(2, type.name());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        bills.add(mapResultSetToBill(rs));
                    }
                }
            }

            return bills;

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error finding bills by date and type", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    @Override
    public long getNextSerialNumber() {
        String sql = "SELECT COALESCE(MAX(bill_id), 0) + 1 FROM bill";
        Connection conn = null;

        try {
            conn = dbConnection.getConnection();

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 1;
            }

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Error getting next serial number", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    /**
     * Helper method to map ResultSet to Bill entity.
     */
    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Long billId = rs.getLong("bill_id");

        Money cashTendered = rs.getBigDecimal("cash_tendered") != null ? new Money(rs.getBigDecimal("cash_tendered"))
                : null;

        // Load bill items for this bill
        List<BillItem> billItems = loadBillItems(billId);

        Bill.Builder builder = new Bill.Builder()
                .serialNumber(rs.getString("serial_number"))
                .dateTime(rs.getTimestamp("bill_date").toLocalDateTime())
                .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                .customerName(rs.getString("customer_name"))
                .items(billItems)
                .paymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")))
                .discount(new Money(rs.getBigDecimal("discount")));

        if (rs.getObject("customer_id") != null) {
            builder.customerId(rs.getLong("customer_id"));
        }

        if (cashTendered != null) {
            builder.cashTendered(cashTendered);
        }

        Bill bill = builder.build();
        bill.setBillId(billId);

        return bill;
    }

    /**
     * Helper method to load bill items for a given bill ID.
     */
    private List<BillItem> loadBillItems(Long billId) throws SQLException {
        List<BillItem> items = new ArrayList<>();
        String sql = "SELECT bi.*, i.name, i.code, i.unit_price, i.discount, i.reorder_level " +
                "FROM bill_item bi JOIN item i ON bi.item_id = i.item_id " +
                "WHERE bi.bill_id = ?";

        Connection conn = null;
        try {
            conn = dbConnection.getConnection();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, billId);

                try (ResultSet rs = stmt.executeQuery()) {
                    ItemRepositoryImpl itemRepo = new ItemRepositoryImpl(dbConnection);

                    while (rs.next()) {
                        // Create Item from the joined data
                        Long itemId = rs.getLong("item_id");
                        Optional<org.syos.domain.entity.Item> itemOpt = itemRepo.findById(itemId);

                        if (itemOpt.isPresent()) {
                            org.syos.domain.entity.Item item = itemOpt.get();
                            BillItem billItem = new BillItem(item, rs.getInt("quantity"));
                            items.add(billItem);
                        }
                    }
                }
            }
        } finally {
            if (conn != null) {
                dbConnection.closeConnection(conn);
            }
        }

        return items;
    }

}
