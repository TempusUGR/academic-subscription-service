package com.calendarugr.academic_subscription_service.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiGatewayValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String forwardedFor = request.getHeader("X-Api-Key");
        if (forwardedFor == null || forwardedFor.isEmpty()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied: Requests must come through the API Gateway.");
            return;
        }

        if (!forwardedFor.equals(System.getProperty("API_KEY"))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied: Invalid API Key.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}