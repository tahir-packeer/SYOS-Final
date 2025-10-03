package org.syos.controller.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.syos.domain.enums.UserRole;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication filter to ensure users are logged in and have appropriate roles
 * for accessing protected resources.
 */
public class AuthenticationFilter implements Filter {

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/index.html", "/login.html", "/register.html", "/css/", "/js/", "/api/auth/login", "/api/auth/register"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // Remove context path from request URI
        String path = requestURI.substring(contextPath.length());

        // Check if path is public (doesn't require authentication)
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check for valid session
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendUnauthorizedResponse(httpResponse, "Not authenticated");
            return;
        }

        // Get user role from session
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null) {
            sendUnauthorizedResponse(httpResponse, "No role found in session");
            return;
        }

        // Check role-based access
        if (!hasRoleAccess(path, userRole)) {
            sendForbiddenResponse(httpResponse, "Insufficient privileges");
            return;
        }

        // User is authenticated and authorized, continue with request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Filter cleanup
    }

    /**
     * Check if the path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath -> 
            path.startsWith(publicPath) || path.equals("/") || path.isEmpty()
        );
    }

    /**
     * Check if user role has access to the requested path
     */
    private boolean hasRoleAccess(String path, String userRole) {
        try {
            UserRole role = UserRole.valueOf(userRole);
            
            // Admin has access to everything
            if (role == UserRole.ADMIN) {
                return true;
            }

            // Manager access
            if (role == UserRole.MANAGER) {
                return !path.startsWith("/admin/");
            }

            // Cashier access
            if (role == UserRole.CASHIER) {
                return path.startsWith("/cashier/") || 
                       path.startsWith("/api/billing/") ||
                       path.startsWith("/api/items/") ||
                       path.startsWith("/api/customers/");
            }

            // Online Customer access
            if (role == UserRole.ONLINE_CUSTOMER) {
                return path.startsWith("/customer/") ||
                       path.startsWith("/api/items/") ||
                       path.startsWith("/api/orders/") ||
                       path.startsWith("/api/online-customers/");
            }

            return false;

        } catch (IllegalArgumentException e) {
            // Invalid role
            return false;
        }
    }

    /**
     * Send 401 Unauthorized response
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"success\": false, \"error\": \"%s\"}", message));
    }

    /**
     * Send 403 Forbidden response
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"success\": false, \"error\": \"%s\"}", message));
    }
}