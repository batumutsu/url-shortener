package com.urlshortener.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse implements Serializable {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private String username;
}
