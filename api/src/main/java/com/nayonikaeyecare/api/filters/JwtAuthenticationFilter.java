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
    private List<String> excludedPathsFromConfig;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private List<String> allExcludedPaths;

    public JwtAuthenticationFilter(JWTTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        // Combine statically configured paths with the dynamic one
        // This ensures that this.allExcludedPaths is initialized after activeProfile is injected.
        // Note: Swagger paths like /swagger-ui/** should use AntPathMatcher for robust matching,
        // but for simplicity with startsWith, we'll keep it as is for now, assuming
        // SecurityConfig handles the more precise matching for Spring Security layer.
        // The primary goal here is to exclude the /<profile>/auth/** path from the JWT token check.
        
        // Ensure activeProfile is available. If not (e.g. in some test contexts without full app context), default.
        String currentProfile = (this.activeProfile != null && !this.activeProfile.isEmpty()) ? this.activeProfile : "dev";
        String dynamicAuthPathPrefix = "/" + currentProfile + "/auth"; 

        this.allExcludedPaths = new java.util.ArrayList<>(excludedPathsFromConfig);
        this.allExcludedPaths.add(dynamicAuthPathPrefix); 
        // Add other paths that SecurityConfig permits and should not be JWT filtered if not already covered by broad patterns
        // For example, if /v3/api-docs, /swagger-ui, /swagger-resources, /actuator are not covered by startsWith logic well enough
        // from application.yml, they could be added here more explicitly or use a PathMatcher.
        // The current `excludedPathsFromConfig` should cover these based on `application.yml`.
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
        String pathString = request.getServletPath();
        String uriString = request.getRequestURI();
        String openAuthPath = "/" + activeProfile + "/auth/";
        String rootAuthPath = "/auth/";
        System.out.println("shouldNotFilter: servletPath=" + pathString + ", requestURI=" + uriString);
        if ((pathString != null && (pathString.startsWith(openAuthPath) || pathString.startsWith(rootAuthPath))) ||
            (uriString != null && (uriString.startsWith(openAuthPath) || uriString.startsWith(rootAuthPath)))) {
            return true;
        }
        return allExcludedPaths.stream().anyMatch(path ->
            (pathString != null && pathString.startsWith(path)) ||
            (uriString != null && uriString.startsWith(path))
        );
    }

}