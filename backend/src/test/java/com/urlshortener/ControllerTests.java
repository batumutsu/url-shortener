package com.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.controller.AuthController;
import com.urlshortener.controller.RedirectController;
import com.urlshortener.controller.UrlController;
import com.urlshortener.dto.*;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.security.JwtAuthenticationFilter;
import com.urlshortener.security.JwtTokenProvider;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ControllerTests {

  @Mock
  private AuthService authService;

  @Mock
  private UrlService urlService;

  @Mock
  private AnalyticsService analyticsService;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @InjectMocks
  private AuthController authController;

  @InjectMocks
  private UrlController urlController;

  @InjectMocks
  private RedirectController redirectController;

  private MockMvc mockMvcAuth;
  private MockMvc mockMvcUrl;
  private ObjectMapper objectMapper;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    mockMvcAuth = MockMvcBuilders.standaloneSetup(authController).build();
    mockMvcUrl = MockMvcBuilders.standaloneSetup(urlController).build();
    MockMvc mockMvcRedirect = MockMvcBuilders.standaloneSetup(redirectController).build();
    objectMapper = new ObjectMapper();
    authentication = new UsernamePasswordAuthenticationToken("testUser", null);
  }

  @Test
  void testRegister_Success() throws Exception {
    RegistrationRequest request = RegistrationRequest.builder().username("testUser").email("test@example.com").password("StrongP@ss123").build();
    AuthResponse response = AuthResponse.builder().accessToken("token").refreshToken("refresh").username("testUser").build();

    when(authService.register(any(RegistrationRequest.class))).thenReturn(response);

    mockMvcAuth.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").value("token"));
  }

  @Test
  void testLogin_Success() throws Exception {
    AuthRequest request = AuthRequest.builder().username("testUser").password("StrongP@ss123").build();
    AuthResponse response = AuthResponse.builder().accessToken("token").refreshToken("refresh").username("testUser").build();

    when(authService.login(any(AuthRequest.class))).thenReturn(response);

    mockMvcAuth.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("token"));
  }

  @Test
  void testRefreshToken_Success() throws Exception {
    AuthResponse response = AuthResponse.builder().accessToken("newToken").refreshToken("newRefresh").username("testUser").build();

    when(authService.refreshToken(anyString())).thenReturn(response);

    mockMvcAuth.perform(post("/auth/refresh").param("refreshToken", "oldRefresh"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("newToken"));
  }

  @Test
  void testLogout_Success() throws Exception {
    mockMvcAuth.perform(post("/auth/logout").header("Authorization", "Bearer token"))
        .andExpect(status().isNoContent());
  }

  @Test
  void testShortenUrl_Success() throws Exception {
    UrlRequest request = UrlRequest.builder().longUrl("https://www.example.com").build();
    UrlResponse response = UrlResponse.builder().shortUrl("short").build();

    when(urlService.createShortUrl(any(UrlRequest.class), anyString())).thenReturn(response);

    mockMvcUrl.perform(post("/urls/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .principal(authentication))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shortUrl").value("short"));
  }

  @Test
  void testGetUserUrls_Success() throws Exception {
    List<UrlResponse> responses = Collections.singletonList(UrlResponse.builder().shortUrl("short").build());

    when(urlService.getUserUrls(anyString())).thenReturn(responses);

    mockMvcUrl.perform(get("/urls").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].shortUrl").value("short"));
  }

  @Test
  void testGetUrl_Success() throws Exception {
    UrlResponse response = UrlResponse.builder().shortUrl("short").build();

    when(urlService.getUrl(anyString(), anyString())).thenReturn(response);

    mockMvcUrl.perform(get("/urls/shortCode").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortUrl").value("short"));
  }

  @Test
  void testGetUrlAnalytics_Success() throws Exception {
    AnalyticsResponse response = AnalyticsResponse.builder().shortCode("short").build();

    when(analyticsService.getUrlAnalytics(anyString(), anyString())).thenReturn(response);

    mockMvcUrl.perform(get("/urls/analytics/shortCode").principal(authentication))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortCode").value("short"));
  }

  @Test
  void testDeleteUrl_Success() throws Exception {
    mockMvcUrl.perform(delete("/urls/shortCode").principal(authentication))
        .andExpect(status().isNoContent());
  }

  @Test
  void testRedirectToOriginalUrl_Success() {
    String shortCode = "testShortCode";
    String longUrl = "https://www.example.com";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("User-Agent", "TestAgent");
    request.addHeader("Referer", "TestReferer");
    request.setRemoteAddr("127.0.0.1");

    when(urlService.getLongUrlAndIncrementClicks(anyString(), anyString(), anyString(), anyString())).thenReturn(longUrl);

    RedirectView redirectView = redirectController.redirectToOriginalUrl(shortCode, request);

    assertEquals(longUrl, redirectView.getUrl());
    assertTrue(redirectView.isRedirectView());
  }
}
