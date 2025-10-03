package org.syos.controller.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.syos.application.repository.ItemRepository;
import org.syos.application.repository.ShelfStockRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.entity.ShelfStock;
import org.syos.domain.enums.UserRole;

import org.syos.infrastructure.persistence.ItemRepositoryImpl;
import org.syos.infrastructure.persistence.ShelfStockRepositoryImpl;
import org.syos.infrastructure.persistence.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Servlet for stock management operations
 */
public class StockServlet extends BaseServlet {
    
    private ItemRepository itemRepository;
    private ShelfStockRepository shelfStockRepository;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize dependencies using existing CCCP1 infrastructure
        DBConnection dbConnection = DBConnection.getInstance();
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
        this.shelfStockRepository = new ShelfStockRepositoryImpl(dbConnection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Only managers and admins can access stock management
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || 
            (currentRole != UserRole.MANAGER && currentRole != UserRole.ADMIN)) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        try {
            switch (pathInfo) {
                case "/all":
                    handleGetAllStock(request, response);
                    break;
                case "/low":
                    handleGetLowStock(request, response);
                    break;
                case "/search":
                    handleSearchStock(request, response);
                    break;
                default:
                    sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Stock GET error", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all items with their stock information
     */
    private void handleGetAllStock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            List<Item> items = itemRepository.findAll();
            List<StockInfo> stockInfoList = new ArrayList<>();
            
            for (Item item : items) {
                Optional<ShelfStock> shelfStockOpt = shelfStockRepository.findByItemCode(item.getCode());
                int currentStock = shelfStockOpt.map(ShelfStock::getQuantity).orElse(0);
                
                stockInfoList.add(new StockInfo(
                    item.getItemId(),
                    item.getName(),
                    item.getCode().getCode(),
                    item.getUnitPrice().getAmount(),
                    currentStock,
                    item.getReorderLevel(),
                    currentStock <= item.getReorderLevel()
                ));
            }
            
            sendSuccessResponse(response, stockInfoList);
            
        } catch (Exception e) {
            logger.error("Get all stock error", e);
            sendErrorResponse(response, "Failed to get stock information", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get items with low stock (below reorder level)
     */
    private void handleGetLowStock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            List<ShelfStock> lowStockItems = shelfStockRepository.findBelowReorderLevel();
            List<StockAlert> alerts = new ArrayList<>();
            
            for (ShelfStock stock : lowStockItems) {
                Optional<Item> itemOpt = itemRepository.findByCode(stock.getItem().getCode());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();
                    alerts.add(new StockAlert(
                        item.getItemId(),
                        item.getName(),
                        item.getCode().getCode(),
                        stock.getQuantity(),
                        item.getReorderLevel(),
                        item.getReorderLevel() - stock.getQuantity()
                    ));
                }
            }
            
            sendSuccessResponse(response, alerts);
            
        } catch (Exception e) {
            logger.error("Get low stock error", e);
            sendErrorResponse(response, "Failed to get low stock information", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search stock by item name
     */
    private void handleSearchStock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String query = request.getParameter("q");
        if (query == null || query.trim().isEmpty()) {
            sendErrorResponse(response, "Search query is required");
            return;
        }

        try {
            List<Item> items = itemRepository.searchByName(query.trim());
            List<StockInfo> stockInfoList = new ArrayList<>();
            
            for (Item item : items) {
                Optional<ShelfStock> shelfStockOpt = shelfStockRepository.findByItemCode(item.getCode());
                int currentStock = shelfStockOpt.map(ShelfStock::getQuantity).orElse(0);
                
                stockInfoList.add(new StockInfo(
                    item.getItemId(),
                    item.getName(),
                    item.getCode().getCode(),
                    item.getUnitPrice().getAmount(),
                    currentStock,
                    item.getReorderLevel(),
                    currentStock <= item.getReorderLevel()
                ));
            }
            
            sendSuccessResponse(response, stockInfoList);
            
        } catch (Exception e) {
            logger.error("Search stock error", e);
            sendErrorResponse(response, "Failed to search stock", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // DTO Classes
    
    public static class StockInfo {
        private Long itemId;
        private String name;
        private String code;
        private BigDecimal price;
        private int currentStock;
        private int reorderLevel;
        private boolean needsRestock;

        public StockInfo(Long itemId, String name, String code, BigDecimal price, 
                        int currentStock, int reorderLevel, boolean needsRestock) {
            this.itemId = itemId;
            this.name = name;
            this.code = code;
            this.price = price;
            this.currentStock = currentStock;
            this.reorderLevel = reorderLevel;
            this.needsRestock = needsRestock;
        }

        // Getters
        public Long getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public BigDecimal getPrice() { return price; }
        public int getCurrentStock() { return currentStock; }
        public int getReorderLevel() { return reorderLevel; }
        public boolean isNeedsRestock() { return needsRestock; }
    }

    public static class StockAlert {
        private Long itemId;
        private String name;
        private String code;
        private int currentStock;
        private int reorderLevel;
        private int shortfall;

        public StockAlert(Long itemId, String name, String code, int currentStock, 
                         int reorderLevel, int shortfall) {
            this.itemId = itemId;
            this.name = name;
            this.code = code;
            this.currentStock = currentStock;
            this.reorderLevel = reorderLevel;
            this.shortfall = shortfall;
        }

        // Getters
        public Long getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public int getCurrentStock() { return currentStock; }
        public int getReorderLevel() { return reorderLevel; }
        public int getShortfall() { return shortfall; }
    }
}