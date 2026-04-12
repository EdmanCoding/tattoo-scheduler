package com.tattoo.scheduler.config;

import com.tattoo.scheduler.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {

            if (!authHeader.startsWith("Bearer ")) {
                log.debug("Invalid Authorization header format (not Bearer) for request: {}",
                        request.getRequestURI());
                sendGenericUnauthorized(response);
                return;
            }

            final String jwt = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractAndValidate(jwt);
                String userEmail = claims.getSubject();

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("Successfully authenticated user: {}", userEmail);

                    } catch (UsernameNotFoundException e) {
                        log.info("Valid JWT but user no longer exists in database: {}", userEmail);
                        sendGenericUnauthorized(response);
                        return;
                    }
                }
            } catch (ExpiredJwtException e) {
                log.debug("Expired JWT token from IP {}: {}", request.getRemoteAddr(), e.getMessage());
                sendGenericUnauthorized(response);
            } catch (JwtException e) {
                log.warn("Invalid JWT token structure: {}", e.getMessage());
                sendGenericUnauthorized(response);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void sendGenericUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid token\"}");
    }
}
