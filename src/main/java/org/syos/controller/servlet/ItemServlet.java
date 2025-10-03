package org.syos.controller.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.syos.application.usecase.ItemAppService;
import org.syos.application.repository.ItemRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.enums.UserRole;
import org.syos.domain.valueobject.ItemCode;
import org.syos.domain.valueobject.Money;
import org.syos.infrastructure.persistence.ItemRepositoryImpl;
import org.syos.infrastructure.persistence.DBConnection;
import org.syos.infrastructure.external.SimpleConsoleLogger;

import java.math.BigDecimal;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet handling item management operations
 */
public class ItemServlet extends BaseServlet {
    
    private ItemAppService itemService;
    private ItemRepository itemRepository;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize dependencies using existing CCCP1 infrastructure
        DBConnection dbConnection = DBConnection.getInstance();
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
        this.itemService = new ItemAppService(itemRepository, new SimpleConsoleLogger());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = getPathInfo(request);
        
        try {
            if (pathInfo.isEmpty() || pathInfo.equals("/")) {
                // GET /api/items - List all items
                handleGetAllItems(request, response);
                
            } else if (pathInfo.startsWith("/search")) {
                // GET /api/items/search?q=searchTerm
                handleSearchItems(request, response);
                
            } else {
                // GET /api/items/{id} - Get item by ID
                Long itemId = extractIdFromPath(request);
                if (itemId != null) {
                    handleGetItemById(request, response, itemId);
                } else {
                    sendErrorResponse(response, "Invalid item ID", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing GET request", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Only Manager and Admin can create items
        UserRole userRole = getCurrentUserRole(request);
        if (userRole != UserRole.MANAGER && userRole != UserRole.ADMIN) {
            sendErrorResponse(response, "Insufficient privileges", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            ItemRequest itemRequest = getRequestBody(request, ItemRequest.class);
            
            if (itemRequest == null || !itemRequest.isValid()) {
                sendErrorResponse(response, "Invalid item data");
                return;
            }

            // Create item using application service
            Item item = itemService.createItem(
                itemRequest.getName(),
                itemRequest.getCode(),
                BigDecimal.valueOf(itemRequest.getUnitPrice()),
                BigDecimal.valueOf(itemRequest.getDiscount()),
                itemRequest.getReorderLevel()
            );

            logger.info("Item created: " + item.getCode().getCode() + " by user: " + getCurrentUserId(request));
            sendSuccessResponse(response, new ItemResponse(item));

        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error creating item", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Only Manager and Admin can update items
        UserRole userRole = getCurrentUserRole(request);
        if (userRole != UserRole.MANAGER && userRole != UserRole.ADMIN) {
            sendErrorResponse(response, "Insufficient privileges", HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        Long itemId = extractIdFromPath(request);
        if (itemId == null) {
            sendErrorResponse(response, "Invalid item ID", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            ItemRequest itemRequest = getRequestBody(request, ItemRequest.class);
            
            if (itemRequest == null || !itemRequest.isValid()) {
                sendErrorResponse(response, "Invalid item data");
                return;
            }

            // Get existing item and update it
            Optional<Item> existingItemOpt = itemRepository.findById(itemId);
            if (existingItemOpt.isEmpty()) {
                sendErrorResponse(response, "Item not found", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Item existingItem = existingItemOpt.get();
            existingItem.setName(itemRequest.getName());
            existingItem.setUnitPrice(new Money(BigDecimal.valueOf(itemRequest.getUnitPrice())));
            existingItem.setDiscount(BigDecimal.valueOf(itemRequest.getDiscount()));
            existingItem.setReorderLevel(itemRequest.getReorderLevel());
            
            itemService.updateItem(existingItem);
            Item updatedItem = existingItem;

            logger.info("Item updated: " + updatedItem.getCode().getCode() + " by user: " + getCurrentUserId(request));
            sendSuccessResponse(response, new ItemResponse(updatedItem));

        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error updating item", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle GET /api/items - List all items
     */
    private void handleGetAllItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Item> items = itemService.getAllItems();
        List<ItemResponse> itemResponses = items.stream()
            .map(ItemResponse::new)
            .toList();
        
        sendSuccessResponse(response, itemResponses);
    }

    /**
     * Handle GET /api/items/search?q=searchTerm
     */
    private void handleSearchItems(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String searchTerm = request.getParameter("q");
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            sendErrorResponse(response, "Search term is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        List<Item> items = itemRepository.searchByName(searchTerm.trim());
        List<ItemResponse> itemResponses = items.stream()
            .map(ItemResponse::new)
            .toList();
        
        sendSuccessResponse(response, itemResponses);
    }

    /**
     * Handle GET /api/items/{id} - Get item by ID
     */
    private void handleGetItemById(HttpServletRequest request, HttpServletResponse response, Long itemId) throws IOException {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        
        if (itemOpt.isEmpty()) {
            sendErrorResponse(response, "Item not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        sendSuccessResponse(response, new ItemResponse(itemOpt.get()));
    }

    /**
     * Item request DTO
     */
    public static class ItemRequest {
        private String name;
        private String code;
        private Double unitPrice;
        private Double discount;
        private Integer reorderLevel;

        // Default constructor for Jackson
        public ItemRequest() {}

        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                   code != null && !code.trim().isEmpty() &&
                   unitPrice != null && unitPrice > 0 &&
                   discount != null && discount >= 0 &&
                   reorderLevel != null && reorderLevel >= 0;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }

        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    }

    /**
     * Item response DTO
     */
    public static class ItemResponse {
        private Long itemId;
        private String name;
        private String code;
        private Double unitPrice;
        private Double discount;
        private Integer reorderLevel;

        public ItemResponse(Item item) {
            this.itemId = item.getItemId();
            this.name = item.getName();
            this.code = item.getCode().getCode();
            this.unitPrice = item.getUnitPrice().getAmount().doubleValue();
            this.discount = item.getDiscount().doubleValue();
            this.reorderLevel = item.getReorderLevel();
        }

        // Getters and setters
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

        public Double getDiscount() { return discount; }
        public void setDiscount(Double discount) { this.discount = discount; }

        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    }
}