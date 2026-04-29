package com.carebridge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final TokenSecurity tokenSecurity = new TokenSecurity();
    
    @Value("${carebridge.jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (tokenSecurity.tokenIsValid(token, secret) && tokenSecurity.tokenNotExpired(token)) {
                    Map<String, Object> userMap = tokenSecurity.getUserWithRolesFromToken(token);
                    String username = (String) userMap.get("username");
                    Set<String> roles = (Set<String>) userMap.get("roles");

                    var authorities = roles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    auth.setDetails(userMap); 
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    request.setAttribute("user", userMap);
                }
            } catch (Exception e) {
                logger.warn("Invalid or expired JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
