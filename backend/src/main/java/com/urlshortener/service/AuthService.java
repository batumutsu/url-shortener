package com.urlshortener.service;

import com.urlshortener.dto.AuthRequest;
import com.urlshortener.dto.AuthResponse;
import com.urlshortener.dto.RegistrationRequest;
import com.urlshortener.entity.User;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UnauthorizedException;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
  }


  // Simple in-memory blacklist for invalidated tokens
  private final Set<String> tokenBlacklist = new HashSet<>();

  @Transactional
  public AuthResponse register(RegistrationRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username is already taken");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();

    userRepository.save(user);

    String accessToken = jwtTokenProvider.createAccessToken(user.getUsername());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .username(user.getUsername())
        .build();
  }

  public AuthResponse login(AuthRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    User user = (User) authentication.getPrincipal();
    String accessToken = jwtTokenProvider.createAccessToken(user.getUsername());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .username(user.getUsername())
        .build();
  }

  public AuthResponse refreshToken(String refreshToken) {
    final String username;

    if (tokenBlacklist.contains(refreshToken)) {
      throw new UnauthorizedException("Token has been invalidated");
    }

    username = jwtTokenProvider.getUsername(refreshToken);

    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

    if (!jwtTokenProvider.validateToken(refreshToken, userDetails)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String newAccessToken = jwtTokenProvider.createAccessToken(username);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(username);

    // Add old refresh token to blacklist
    tokenBlacklist.add(refreshToken);

    return AuthResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .username(user.getUsername())
        .build();
  }

  public void logout(String token) {
    tokenBlacklist.add(token);
  }
}
