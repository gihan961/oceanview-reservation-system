package com.oceanview.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/api/logout"})
public class LogoutServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        performLogout(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        performLogout(request, response);
    }
    
    private void performLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                String username = (String) session.getAttribute("username");
                session.invalidate();
                LOGGER.info("User logged out: " + (username != null ? username : "unknown"));
                
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{");
                out.print("\"success\": true,");
                out.print("\"message\": \"Logout successful\"");
                out.print("}");
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{");
                out.print("\"success\": true,");
                out.print("\"message\": \"No active session found\"");
                out.print("}");
            }
            
        } catch (Exception e) {
            LOGGER.severe("Error during logout: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{");
            out.print("\"success\": false,");
            out.print("\"message\": \"Error during logout\"");
            out.print("}");
        } finally {
            out.flush();
        }
    }
}
