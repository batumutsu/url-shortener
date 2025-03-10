package com.urlshortener.controller;

import com.urlshortener.dto.AuthRequest;
import com.urlshortener.dto.AuthResponse;
import com.urlshortener.dto.RegistrationRequest;
import com.urlshortener.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
    AuthResponse response = authService.refreshToken(refreshToken);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
    authService.logout(token.substring(7));
    return ResponseEntity.ok().build();
  }
}
