package com.urlshortener.security;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    log.debug("JwtAuthenticationFilter called for URI: {}", request.getRequestURI());
    try {
      final String jwt;
      final String userName;

      jwt = jwtTokenProvider.getJwtFromRequest(request);
      userName = jwt != null ? jwtTokenProvider.getUsername(jwt) : null;

      if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

        if (jwtTokenProvider.validateToken(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );
          authToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request)
          );
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (Exception e) {
      log.error("Error during filtering: {}", e.getMessage());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

      final Map<String, Object> body = new HashMap<>();
      body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
      body.put("error", "Error");
      body.put("message", e.getMessage());
      body.put("path", request.getServletPath());

      final ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(response.getOutputStream(), body);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
