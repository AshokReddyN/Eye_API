package com.nayonikaeyecare.api.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.lang.NonNull;
import com.nayonikaeyecare.api.security.JWTTokenProvider;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
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

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private List<String> allExcludedPaths;

    @PostConstruct
    public void initExcludedPaths() {
        this.allExcludedPaths = new ArrayList<>(excludedPaths);
    }

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

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If the path is not excluded and auth header is missing/invalid, then it's an
            // unauthorized request.
            // For excluded paths, shouldNotFilter would have returned true, and this filter
            // wouldn't run.
            // However, if an excluded path somehow reaches here without a token (e.g.
            // misconfiguration),
            // and we want to strictly enforce JWT for non-excluded paths, this check is
            // fine.
            // If the intent is to allow some non-excluded paths to proceed without JWT,
            // then this response should only be sent if
            // SecurityContextHolder.getContext().getAuthentication() is null
            // and the path is supposed to be protected.
            // For now, strict checking: if header is bad, it's likely an attempt to access
            // a protected resource without proper auth.
            if (!shouldNotFilter(request)) { // Double check, though shouldNotFilter should prevent this for excluded
                                             // paths
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authorization header missing or invalid");
                return;
            }
            // If it's an excluded path, just continue the chain.
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            username = jwtTokenProvider.extractUsername(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // This case might be redundant if validateToken itself throws an exception for
                    // invalid tokens.
                    // However, if validateToken returns false for other reasons (e.g. user
                    // disabled, not just token format/expiry)
                    // keeping a generic unauthorized might be desired. For this task, we focus on
                    // JWT exceptions.
                    // sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT
                    // token");
                    // return;
                    // Assuming JWT exceptions below will cover most "invalid" scenarios.
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT expired");
        } catch (UnsupportedJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unsupported JWT token");
        } catch (MalformedJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token");
        } catch (SignatureException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT signature");
        } catch (IllegalArgumentException e) {
            // This can be thrown if the token string is null or empty, or issues with
            // parsing claims
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT argument");
        } catch (Exception e) {
            // Catch-all for any other unexpected exceptions during JWT processing
            logger.error("Unexpected error during JWT processing", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing JWT token");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        // Using ObjectMapper to create JSON for consistency and to handle special
        // characters in message if any.
        // Simple string concatenation for {"error": "message"} is also fine if
        // ObjectMapper is not readily available/desired.
        // For this example, let's assume a simple structure.
        // String errorJson = String.format("{\"error\": \"%s\"}", message);

        // Using ObjectMapper for robust JSON creation
        ObjectMapper mapper = new ObjectMapper();
        String errorJson = mapper.writeValueAsString(java.util.Map.of("error", message));

        response.getWriter().write(errorJson);
        response.getWriter().flush(); // Ensure the response is sent
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
        return allExcludedPaths.stream().anyMatch(path -> (pathString != null && pathString.startsWith(path)) ||
                (uriString != null && uriString.startsWith(path)));
    }

}