package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Collection;

@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Default redirect path
        String redirectUrl = "/auth/login/user";
        
        if (authentication != null) {
            // Check for session attributes that might indicate user type
            HttpSession session = request.getSession(false);
            if (session != null) {
                // Check if we have organization or admin session attributes
                if (session.getAttribute("org_email") != null) {
                    redirectUrl = "/auth/login/organisation";
                } else if (session.getAttribute("admin_email") != null) {
                    redirectUrl = "/auth/login/superadmin";
                }
            }
            
            // If no session attributes found, fall back to role-based check
            if (redirectUrl.equals("/auth/login/user")) {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                if (authorities != null) {
                    for (GrantedAuthority authority : authorities) {
                        String role = authority.getAuthority();
                        System.out.println("User has role: " + role);
                        
                        if ("ROLE_SUPER_ADMIN".equals(role)) {
                            redirectUrl = "/auth/login/superadmin";
                            break;
                        } else if ("ROLE_ORGANISATION".equals(role)) {
                            redirectUrl = "/auth/login/organisation";
                            break;
                        }
                    }
                }
            }
            
            // Log the final redirect before logout
            System.out.println("Redirecting to: " + redirectUrl);
            
            // Perform logout
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // Invalidate session manually as well
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to appropriate login page based on role
        return "redirect:" + redirectUrl;
    }
}