package org.syos.controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.syos.application.service.Logger;
import org.syos.domain.enums.UserRole;
import org.syos.infrastructure.external.SimpleConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base servlet class providing common functionality for all servlets.
 * Implements Template Method pattern for request handling.
 */
public abstract class BaseServlet extends HttpServlet {
    
    protected final ObjectMapper objectMapper;
    protected final Logger logger;

    public BaseServlet() {
        this.objectMapper = new ObjectMapper();
        this.logger = new SimpleConsoleLogger(); // Will be injected properly later
    }

    /**
     * Get JSON request body as object
     */
    protected <T> T getRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder jsonBuffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonBuffer.append(line);
        }
        
        String json = jsonBuffer.toString();
        if (json.trim().isEmpty()) {
            return null;
        }
        
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Send JSON response
     */
    protected void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        objectMapper.writeValue(writer, data);
        writer.flush();
    }

    /**
     * Send success response
     */
    protected void sendSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        ApiResponse apiResponse = new ApiResponse(true, data, null);
        sendJsonResponse(response, apiResponse);
    }

    /**
     * Send error response
     */
    protected void sendErrorResponse(HttpServletResponse response, String errorMessage, int statusCode) throws IOException {
        response.setStatus(statusCode);
        ApiResponse apiResponse = new ApiResponse(false, null, errorMessage);
        sendJsonResponse(response, apiResponse);
    }

    /**
     * Send error response with default 400 status
     */
    protected void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        sendErrorResponse(response, errorMessage, HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Get current user ID from session
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        Object userId = session.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    /**
     * Get current user role from session
     */
    protected UserRole getCurrentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        String roleString = (String) session.getAttribute("userRole");
        if (roleString == null) {
            return null;
        }
        
        try {
            return UserRole.valueOf(roleString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get path info from request (last part after servlet path)
     */
    protected String getPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return pathInfo != null ? pathInfo : "";
    }

    /**
     * Extract ID from path (e.g., /api/items/123 -> 123)
     */
    protected Long extractIdFromPath(HttpServletRequest request) {
        String pathInfo = getPathInfo(request);
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        
        if (pathInfo.isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(pathInfo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * API Response wrapper class
     */
    public static class ApiResponse {
        private boolean success;
        private Object data;
        private String error;

        public ApiResponse(boolean success, Object data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Session data class
     */
    public static class SessionData {
        private Long userId;
        private String username;
        private String fullName;
        private UserRole role;

        public SessionData(Long userId, String username, String fullName, UserRole role) {
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
        }

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }
}