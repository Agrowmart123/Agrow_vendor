package com.agrowmart.config;


import com.agrowmart.admin_seller_management.repository.AdminRepository;
import com.agrowmart.repository.UserRepository;
import com.agrowmart.repository.customer.CustomerRepository;
import com.agrowmart.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component

public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminRepository adminRepository;
    
    
    public AdminJwtAuthenticationFilter(JwtUtil jwtUtil, AdminRepository adminRepository
         ) {
this.jwtUtil = jwtUtil;
this.adminRepository=adminRepository;

}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (jwtUtil.isAdminToken(token)) {
            Long adminId = jwtUtil.extractAdminId(token);

            if (adminId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                adminRepository.findById(adminId).ifPresent(admin -> {
                    if (!admin.isDeleted() && admin.isActive()) {
                        List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority(admin.getRole().name())
                        );
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                admin, null, authorities
                        );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                });
            }
        }

        chain.doFilter(request, response);
    }
}