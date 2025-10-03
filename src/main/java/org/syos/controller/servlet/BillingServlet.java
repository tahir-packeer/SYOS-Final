package org.syos.controller.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.syos.application.repository.ItemRepository;
import org.syos.application.repository.CustomerRepository;
import org.syos.domain.entity.Item;
import org.syos.domain.entity.Customer;
import org.syos.domain.enums.UserRole;
import org.syos.infrastructure.persistence.ItemRepositoryImpl;
import org.syos.infrastructure.persistence.CustomerRepositoryImpl;
import org.syos.infrastructure.persistence.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * Simplified servlet for billing operations - search functionality
 */
public class BillingServlet extends BaseServlet {
    
    private ItemRepository itemRepository;
    private CustomerRepository customerRepository;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize dependencies using existing CCCP1 infrastructure
        DBConnection dbConnection = DBConnection.getInstance();
        this.itemRepository = new ItemRepositoryImpl(dbConnection);
        this.customerRepository = new CustomerRepositoryImpl(dbConnection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Only cashiers and managers can access billing
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || 
            (currentRole != UserRole.CASHIER && currentRole != UserRole.MANAGER && currentRole != UserRole.ADMIN)) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        try {
            switch (pathInfo) {
                case "/items/search":
                    handleItemSearch(request, response);
                    break;
                case "/customers/search":
                    handleCustomerSearch(request, response);
                    break;
                default:
                    sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Billing GET error", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search for items by name for billing
     */
    private void handleItemSearch(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String query = request.getParameter("q");
        if (query == null || query.trim().isEmpty()) {
            sendErrorResponse(response, "Search query is required");
            return;
        }

        try {
            List<Item> items = itemRepository.searchByName(query.trim());
            List<ItemSearchResult> results = new ArrayList<>();
            
            for (Item item : items) {
                results.add(new ItemSearchResult(
                    item.getItemId(),
                    item.getName(),
                    item.getCode().getCode(),
                    item.getUnitPrice().getAmount(),
                    item.getDiscount()
                ));
            }
            
            sendSuccessResponse(response, results);
            
        } catch (Exception e) {
            logger.error("Item search error", e);
            sendErrorResponse(response, "Failed to search items", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search for customers by name for billing
     */
    private void handleCustomerSearch(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String query = request.getParameter("q");
        if (query == null || query.trim().isEmpty()) {
            sendErrorResponse(response, "Search query is required");
            return;
        }

        try {
            List<Customer> customers = customerRepository.searchByName(query.trim());
            List<CustomerSearchResult> results = new ArrayList<>();
            
            for (Customer customer : customers) {
                results.add(new CustomerSearchResult(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhone()
                ));
            }
            
            sendSuccessResponse(response, results);
            
        } catch (Exception e) {
            logger.error("Customer search error", e);
            sendErrorResponse(response, "Failed to search customers", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Simple DTO classes for responses
    
    public static class ItemSearchResult {
        private Long itemId;
        private String name;
        private String code;
        private BigDecimal price;
        private BigDecimal discount;

        public ItemSearchResult(Long itemId, String name, String code, BigDecimal price, BigDecimal discount) {
            this.itemId = itemId;
            this.name = name;
            this.code = code;
            this.price = price;
            this.discount = discount;
        }

        // Getters
        public Long getItemId() { return itemId; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getDiscount() { return discount; }
    }

    public static class CustomerSearchResult {
        private Long customerId;
        private String name;
        private String phone;

        public CustomerSearchResult(Long customerId, String name, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
        }

        // Getters
        public Long getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
    }
}