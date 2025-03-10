package com.urlshortener.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import redis.clients.jedis.Jedis;
import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  @Value("${rate.limit.capacity}")
  private int MAX_REQUESTS;

  @Value("${spring.redis.host}")
  private String REDIS_HOST;

  @Value("${spring.redis.port}")
  private int REDIS_PORT;

  // Time windows in seconds
  @Value("${rate.limit.authenticated.window}")
  private int AUTHENTICATED_WINDOW;

  @Value("${rate.limit.unauthenticated.window}")
  private int UNAUTHENTICATED_WINDOW;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = request.getRemoteAddr();
    String requestPath = request.getRequestURI();

    // Check if user is authenticated
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated = authentication != null && authentication.isAuthenticated() &&
        !authentication.getPrincipal().equals("anonymousUser");

    // Create different Redis keys based on authentication status
    String key;
    int timeWindow;

    if (isAuthenticated) {
      // For authenticated users: rate limit per user per endpoint
      String username = authentication.getName();
      key = "rate_limit:auth:" + username + ":" + requestPath;
      timeWindow = AUTHENTICATED_WINDOW;
    } else {
      // For unauthenticated users: rate limit per IP address for all endpoints
      key = "rate_limit:unauth:" + clientIp;
      timeWindow = UNAUTHENTICATED_WINDOW;
    }

    try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
      String requestCount = jedis.get(key);

      if (requestCount == null) {
        // First request, set count to 1 and set expiration based on authentication status
        jedis.setex(key, timeWindow, "1");
      } else {
        int currentCount = Integer.parseInt(requestCount);

        if (isAuthenticated &&currentCount < MAX_REQUESTS) {
          // Increment the request count
          jedis.incr(key);
        } else {
          // Limit exceeded, send 429 Too Many Requests response
          response.setStatus(429);
          response.getWriter().write("Rate limit exceeded. Try again later.");
          return;
        }
      }
    }

    filterChain.doFilter(request, response); // Continue the request
  }
}

