package com.nayonikaeyecare.api.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mongodb.lang.NonNull;
import com.nayonikaeyecare.api.security.JWTTokenProvider;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Value("${auth.excluded.path:#{T(java.util.Collections).emptyList()}}")
    private List<String> excludedPaths;

    public JwtAuthenticationFilter(JWTTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is called for every request to check if the request contains a
     * valid JWT token.
     * If the token is valid, it sets the authentication in the security context.
     * 
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs
     */

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Implement JWT authentication logic here
        // For example, extract the token from the request header and validate it
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Unauthorized request\"}");
            return;
        }
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // Validate the token and set the authentication in the security context

            final String username = jwtTokenProvider.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Specify the paths that should not be filtered
        String pathString = request.getServletPath();
        boolean ignoreFilter = excludedPaths.stream().anyMatch(path -> pathString.startsWith(path));
        return ignoreFilter;
    }

}