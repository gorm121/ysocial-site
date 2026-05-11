package com.ysocial.org.ysocialsite.security;

import com.ysocial.org.ysocialsite.entites.User;
import com.ysocial.org.ysocialsite.enums.AccountStatus;
import com.ysocial.org.ysocialsite.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class StatusCheckFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    public StatusCheckFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {

            Optional<User> userOptional = userRepository.findById(userDetails.getId());
            if (userOptional.isPresent() && userOptional.get().getStatus() == AccountStatus.BANNED) {
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                response.sendRedirect("/login?banned=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
