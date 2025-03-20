package com.urlshortener.security;

import com.urlshortener.config.RedisConfig;
import com.urlshortener.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

  @Value("${rate.limit.capacity}")
  private int MAX_REQUESTS;

  @Value("${rate.limit.authenticated.window}")
  private int AUTHENTICATED_WINDOW;

  @Value("${rate.limit.unauthenticated.window}")
  private int UNAUTHENTICATED_WINDOW;

  // Inject the Redis service
  private final RedisConfig redisConfig;

  private final JwtTokenProvider jwtTokenProvider;

  // Paths that should be excluded from rate limiting
  private static final List<String> EXCLUDED_PATHS = Arrays.asList(
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/swagger-resources/**",
      "/webjars/**"
  );

  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return EXCLUDED_PATHS.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
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
      String username = authentication.getName();
      key = "rate_limit:auth:" + username + ":" + requestPath;
      timeWindow = AUTHENTICATED_WINDOW;
    } else {
      key = "rate_limit:unauth:" + clientIp;
      timeWindow = UNAUTHENTICATED_WINDOW;
    }

    String requestCount = redisConfig.get(key);

    if (requestCount == null) {
      redisConfig.setex(key, timeWindow, "1");
    } else {
      int currentCount = Integer.parseInt(requestCount);

      if (currentCount >= MAX_REQUESTS) {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("Rate limit exceeded. Try again later.");
        return;
      }

      redisConfig.incr(key);
    }

    filterChain.doFilter(request, response);
  }
}