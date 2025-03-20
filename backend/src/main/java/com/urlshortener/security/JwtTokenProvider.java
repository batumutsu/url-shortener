package com.urlshortener.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenProvider {

  private final UserDetailsService userDetailsService;

  public JwtTokenProvider(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.accessToken.expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refreshToken.expiration}")
  private long refreshTokenExpiration;

  private SecretKey key;

  @PostConstruct
  protected void init() {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(String username) {
    return createAccessToken(new HashMap<>(), username);
  }

  public String createAccessToken(Map<String, Object> extraClaims,String username) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .claims(extraClaims)
        .subject(username)
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(String username) {
    return createRefreshToken(new HashMap<>(), username);
  }

  public String createRefreshToken(Map<String, Object> extraClaims,String username) {
    extraClaims.put("type", "refresh");
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .claims(extraClaims)
        .subject(username)
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    String username = getUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parser()
        .verifyWith((SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = getUsername(token);
      return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }

  public boolean isTokenExpired(String token) {
    final Date expiration = extractExpiration(token);
    return expiration.before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
