package org.syos.controller.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.syos.application.usecase.AuthenticationAppService;
import org.syos.application.repository.UserRepository;
import org.syos.domain.entity.User;
import org.syos.domain.enums.UserRole;
import org.syos.infrastructure.persistence.UserRepositoryImpl;
import org.syos.infrastructure.persistence.DBConnection;
import org.syos.infrastructure.external.SimpleConsoleLogger;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet handling user authentication (login/logout)
 */
public class AuthenticationServlet extends BaseServlet {
    
    private AuthenticationAppService authService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Initialize dependencies using existing CCCP1 infrastructure
        DBConnection dbConnection = DBConnection.getInstance();
        UserRepository userRepository = new UserRepositoryImpl(dbConnection);
        this.authService = new AuthenticationAppService(userRepository, new SimpleConsoleLogger());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = getPathInfo(request);
        
        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/logout":
                handleLogout(request, response);
                break;
            default:
                sendErrorResponse(response, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Handle user login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Get login credentials from request
            LoginRequest loginRequest = getRequestBody(request, LoginRequest.class);
            
            if (loginRequest == null || !loginRequest.isValid()) {
                sendErrorResponse(response, "Invalid login credentials");
                return;
            }

            // Authenticate user
            Optional<User> userOpt = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            
            if (userOpt.isEmpty()) {
                sendErrorResponse(response, "Invalid username or password", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User user = userOpt.get();
            
            // Validate user type matches requested role
            if (!user.getRole().name().equals(loginRequest.getUserType())) {
                sendErrorResponse(response, "Invalid user role", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Create session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("userRole", user.getRole().name());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Prepare response data
            SessionData sessionData = new SessionData(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
            );

            logger.info("User logged in: " + user.getUsername() + " (" + user.getRole() + ")");
            sendSuccessResponse(response, sessionData);

        } catch (Exception e) {
            logger.error("Login error", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handle user logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                String username = (String) session.getAttribute("username");
                session.invalidate();
                
                if (username != null) {
                    logger.info("User logged out: " + username);
                }
            }

            sendSuccessResponse(response, "Logged out successfully");

        } catch (Exception e) {
            logger.error("Logout error", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String username;
        private String password;
        private String userType;

        // Default constructor for Jackson
        public LoginRequest() {}

        public LoginRequest(String username, String password, String userType) {
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        public boolean isValid() {
            return username != null && !username.trim().isEmpty() &&
                   password != null && !password.trim().isEmpty() &&
                   userType != null && !userType.trim().isEmpty();
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
    }
}