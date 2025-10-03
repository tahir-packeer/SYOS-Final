package org.syos.controller.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.syos.application.repository.CustomerRepository;
import org.syos.domain.entity.Customer;
import org.syos.domain.enums.UserRole;
import org.syos.infrastructure.persistence.CustomerRepositoryImpl;
import org.syos.infrastructure.persistence.DBConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for customer management operations
 */
public class CustomerServlet extends BaseServlet {

    private CustomerRepository customerRepository;

    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize dependencies using existing CCCP1 infrastructure
        DBConnection dbConnection = DBConnection.getInstance();
        this.customerRepository = new CustomerRepositoryImpl(dbConnection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication - cashiers, managers, and admins can manage customers
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || 
            (currentRole != UserRole.CASHIER && currentRole != UserRole.MANAGER && currentRole != UserRole.ADMIN)) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        switch (pathInfo) {
            case "/all":
                handleGetAllCustomers(request, response);
                break;
            case "/search":
                handleSearchCustomers(request, response);
                break;
            default:
                // Check if it's a specific customer ID
                if (pathInfo.matches("^/\\d+$")) {
                    handleGetCustomer(request, response, pathInfo.substring(1));
                } else {
                    sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
                }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication - cashiers, managers, and admins can add customers
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || 
            (currentRole != UserRole.CASHIER && currentRole != UserRole.MANAGER && currentRole != UserRole.ADMIN)) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        if ("/add".equals(pathInfo)) {
            handleAddCustomer(request, response);
        } else {
            sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication - managers and admins can update customers
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || 
            (currentRole != UserRole.MANAGER && currentRole != UserRole.ADMIN)) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        // Check if it's a specific customer ID for update
        if (pathInfo.matches("^/\\d+$")) {
            handleUpdateCustomer(request, response, pathInfo.substring(1));
        } else {
            sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check authentication - only admins can delete customers
        UserRole currentRole = getCurrentUserRole(request);
        if (currentRole == null || currentRole != UserRole.ADMIN) {
            sendErrorResponse(response, "Unauthorized access", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = getPathInfo(request);
        
        // Check if it's a specific customer ID for deletion
        if (pathInfo.matches("^/\\d+$")) {
            handleDeleteCustomer(request, response, pathInfo.substring(1));
        } else {
            sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Get all customers
     */
    private void handleGetAllCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            List<Customer> customers = customerRepository.findAll();
            List<CustomerInfo> customerInfoList = new ArrayList<>();
            
            for (Customer customer : customers) {
                customerInfoList.add(new CustomerInfo(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhone()
                ));
            }
            
            sendSuccessResponse(response, customerInfoList);
            
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to retrieve customers: " + e.getMessage());
        }
    }

    /**
     * Search customers by name
     */
    private void handleSearchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String query = request.getParameter("q");
        if (query == null || query.trim().isEmpty()) {
            sendErrorResponse(response, "Search query is required");
            return;
        }

        try {
            List<Customer> customers = customerRepository.searchByName(query.trim());
            List<CustomerSearchResult> searchResults = new ArrayList<>();
            
            for (Customer customer : customers) {
                searchResults.add(new CustomerSearchResult(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhone()
                ));
            }
            
            sendSuccessResponse(response, searchResults);
            
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to search customers: " + e.getMessage());
        }
    }

    /**
     * Get specific customer by ID
     */
    private void handleGetCustomer(HttpServletRequest request, HttpServletResponse response, String customerIdStr) 
            throws IOException {
        
        try {
            Long customerId = Long.parseLong(customerIdStr);
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                CustomerInfo customerInfo = new CustomerInfo(
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhone()
                );
                sendSuccessResponse(response, customerInfo);
            } else {
                sendErrorResponse(response, "Customer not found", HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid customer ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to retrieve customer: " + e.getMessage());
        }
    }

    /**
     * Add new customer
     */
    private void handleAddCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            CustomerRequest customerRequest = getRequestBody(request, CustomerRequest.class);
            
            if (customerRequest == null || !customerRequest.isValid()) {
                sendErrorResponse(response, "Invalid customer data");
                return;
            }

            // Create new customer (only name and phone as per Customer entity)
            Customer customer = new Customer(
                customerRequest.getName(),
                customerRequest.getPhone()
            );

            Customer savedCustomer = customerRepository.save(customer);
            
            CustomerInfo customerInfo = new CustomerInfo(
                savedCustomer.getCustomerId(),
                savedCustomer.getName(),
                savedCustomer.getPhone()
            );
            
            sendSuccessResponse(response, customerInfo);
            
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to add customer: " + e.getMessage());
        }
    }

    /**
     * Update existing customer
     */
    private void handleUpdateCustomer(HttpServletRequest request, HttpServletResponse response, String customerIdStr) 
            throws IOException {
        
        try {
            Long customerId = Long.parseLong(customerIdStr);
            CustomerRequest customerRequest = getRequestBody(request, CustomerRequest.class);
            
            if (customerRequest == null || !customerRequest.isValid()) {
                sendErrorResponse(response, "Invalid customer data");
                return;
            }

            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (customerOpt.isEmpty()) {
                sendErrorResponse(response, "Customer not found", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Customer customer = customerOpt.get();
            customer.setName(customerRequest.getName());
            customer.setPhone(customerRequest.getPhone());

            customerRepository.update(customer);
            
            CustomerInfo customerInfo = new CustomerInfo(
                customer.getCustomerId(),
                customer.getName(),
                customer.getPhone()
            );
            
            sendSuccessResponse(response, customerInfo);
            
        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid customer ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to update customer: " + e.getMessage());
        }
    }

    /**
     * Delete customer
     */
    private void handleDeleteCustomer(HttpServletRequest request, HttpServletResponse response, String customerIdStr) 
            throws IOException {
        
        try {
            Long customerId = Long.parseLong(customerIdStr);
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            
            if (customerOpt.isEmpty()) {
                sendErrorResponse(response, "Customer not found", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            customerRepository.delete(customerId);
            
            sendSuccessResponse(response, "Customer deleted successfully");
            
        } catch (NumberFormatException e) {
            sendErrorResponse(response, "Invalid customer ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(response, "Failed to delete customer: " + e.getMessage());
        }
    }

    /**
     * Customer information DTO
     */
    public static class CustomerInfo {
        private Long customerId;
        private String name;
        private String phone;

        public CustomerInfo(Long customerId, String name, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
        }

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * Customer search result DTO
     */
    public static class CustomerSearchResult {
        private Long customerId;
        private String name;
        private String phone;

        public CustomerSearchResult(Long customerId, String name, String phone) {
            this.customerId = customerId;
            this.name = name;
            this.phone = phone;
        }

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * Customer request DTO for add/update operations
     */
    public static class CustomerRequest {
        private String name;
        private String phone;

        public CustomerRequest() {}

        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                   phone != null && !phone.trim().isEmpty();
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}