package com.drawsync.backend.security;

import com.drawsync.backend.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends HttpFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain)
            throws IOException, ServletException {

        String path = request.getRequestURI();

        if (path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String email = jwtService.extractEmail(token);

            // ✅ 1. Extract the role string from the JWT (e.g., "HOST", "EDITOR")
            String role = jwtService.extractRole(token);

            // ✅ 2. Convert the role string into a GrantedAuthority format Spring expects ("ROLE_HOST")
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // ✅ 3. Inject the real authorities list instead of Collections.emptyList()
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            request.setAttribute("userEmail", email);

        } catch (Exception e) {
            response.setStatus(401);
            return;
        }

        chain.doFilter(request, response);
    }
}