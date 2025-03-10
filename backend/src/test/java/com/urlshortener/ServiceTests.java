package com.urlshortener;

import com.urlshortener.dto.*;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UnauthorizedException;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtAuthenticationFilter;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.UrlService;
import com.urlshortener.util.ShortCodeGenerator;
import com.urlshortener.util.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceTests {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UrlRepository urlRepository;

  @Mock
  private ClickAnalyticsRepository clickAnalyticsRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private ShortCodeGenerator shortCodeGenerator;

  @Mock
  private UrlValidator urlValidator;

  @InjectMocks
  private AuthService authService;

  @InjectMocks
  private UrlService urlService;

  @InjectMocks
  private AnalyticsService analyticsService;

  private User testUser;
  private Url testUrl;
  private ClickAnalytics testClickAnalytics;

  @BeforeEach
  void setUp() {
    testUser = User.builder().username("testUser").email("test@example.com").password("encodedPassword").build();
    testUrl = Url.builder().user(testUser).shortCode("short").longUrl("https://www.example.com").clicks(0).build();
    testClickAnalytics = ClickAnalytics.builder().url(testUrl).clickedAt(LocalDateTime.now()).build();

    testUser.setId(UUID.randomUUID());
    testUrl.setId(UUID.randomUUID());
    testClickAnalytics.setId(UUID.randomUUID());
  }

  @Test
  void testRegister_Success() {
    RegistrationRequest request = RegistrationRequest.builder().username("testUser").email("test@example.com").password("password").build();
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtTokenProvider.createAccessToken(anyString())).thenReturn("accessToken");
    when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("refreshToken");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals("accessToken", response.getAccessToken());
  }

  @Test
  void testLogin_Success() {
    AuthRequest request = AuthRequest.builder().username("testUser").password("password").build();
    Authentication authentication = new UsernamePasswordAuthenticationToken(testUser, null);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(jwtTokenProvider.createAccessToken(anyString())).thenReturn("accessToken");
    when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("refreshToken");

    AuthResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("accessToken", response.getAccessToken());
  }

  @Test
  void testRefreshToken_Success() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder().username("testUser").password("encodedPassword").roles("USER").build();
    when(jwtTokenProvider.getUsername(anyString())).thenReturn("testUser");
    when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
    when(jwtTokenProvider.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
    when(jwtTokenProvider.createAccessToken(anyString())).thenReturn("newAccessToken");
    when(jwtTokenProvider.createRefreshToken(anyString())).thenReturn("newRefreshToken");

    AuthResponse response = authService.refreshToken("refreshToken");

    assertNotNull(response);
    assertEquals("newAccessToken", response.getAccessToken());
  }

  @Test
  void testCreateShortUrl_Success() {
    UrlRequest request = UrlRequest.builder().longUrl("https://www.example.com").build();
    when(urlValidator.isValidUrl(anyString())).thenReturn(true);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
    when(urlRepository.findByLongUrlAndUser(anyString(), any(User.class))).thenReturn(Optional.empty());
    when(shortCodeGenerator.generate(anyInt())).thenReturn("short");
    when(urlRepository.existsByShortCode(anyString())).thenReturn(false);
    when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

    UrlResponse response = urlService.createShortUrl(request, "testUser");

    assertNotNull(response);
    assertEquals("short", response.getShortCode());
  }

  @Test
  void testGetUserUrls_Success() {
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
    when(urlRepository.findByUser(any(User.class))).thenReturn(Collections.singletonList(testUrl));

    List<UrlResponse> responses = urlService.getUserUrls("testUser");

    assertFalse(responses.isEmpty());
    assertEquals("short", responses.get(0).getShortCode());
  }

  @Test
  void testGetUrl_Success() {
    when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(testUrl));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

    UrlResponse response = urlService.getUrl("short", "testUser");

    assertNotNull(response);
    assertEquals("short", response.getShortCode());
  }

  @Test
  void testGetLongUrlAndIncrementClicks_Success() {
    when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(testUrl));

    String longUrl = urlService.getLongUrlAndIncrementClicks("short", "referrer", "userAgent", "ipAddress");

    assertEquals("https://www.example.com", longUrl);
    verify(urlRepository, times(1)).incrementClicks("short");
    verify(clickAnalyticsRepository, times(1)).save(any(ClickAnalytics.class));
  }

  @Test
  void testDeleteUrl_Success() {
    when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(testUrl));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

    assertDoesNotThrow(() -> urlService.deleteUrl("short", "testUser"));
    verify(urlRepository, times(1)).delete(any(Url.class));
  }

  @Test
  void testGetUrlAnalytics_Success() {
    when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.of(testUrl));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
    when(clickAnalyticsRepository.findByUrl(any(Url.class))).thenReturn(Collections.singletonList(testClickAnalytics));

    AnalyticsResponse response = analyticsService.getUrlAnalytics("short", "testUser");

    assertNotNull(response);
    assertEquals("short", response.getShortCode());
  }
}
