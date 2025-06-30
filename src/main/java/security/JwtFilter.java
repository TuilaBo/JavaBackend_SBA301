package security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SystemAccountService;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final SystemAccountService systemAccountService;
    private final JwtUtil jwtUtil;

    public JwtFilter(SystemAccountService systemAccountService, JwtUtil jwtUtil) {
        this.systemAccountService = systemAccountService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        
        logger.info("Processing request: {} with Authorization header: {}", requestURI, authorizationHeader != null ? "present" : "missing");

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            email = jwtUtil.extractEmail(jwt);
            logger.info("Extracted email from JWT: {}", email);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, email)) {
                logger.info("JWT token is valid for email: {}", email);
                try {
                    // Load user details from database to get proper authorities
                    UserDetails userDetails = systemAccountService.loadUserByUsername(email);
                    logger.info("Loaded user details for {} with authorities: {}", email, userDetails.getAuthorities());
                    
                    AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.info("Authentication set in SecurityContext for user: {}", email);
                } catch (Exception e) {
                    logger.error("Error loading user details for email: {}", email, e);
                }
            } else {
                logger.warn("JWT token validation failed for email: {}", email);
            }
        } else if (email == null) {
            logger.debug("No email extracted from JWT token");
        } else {
            logger.debug("Authentication already exists in SecurityContext for email: {}", email);
        }

        filterChain.doFilter(request, response);
    }
}