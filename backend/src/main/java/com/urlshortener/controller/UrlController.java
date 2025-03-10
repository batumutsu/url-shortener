package com.urlshortener.controller;

import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/urls")
@RequiredArgsConstructor
public class UrlController {

  private final UrlService urlService;
  private final AnalyticsService analyticsService;

  @PostMapping("/shorten")
  @Operation(summary = "Create a shortened URL", description = "Creates a new shortened URL from the provided original URL")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", content = { @Content(schema = @Schema(implementation = UrlRequest.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "400", description = "Invalid URL", content = @Content),
      @ApiResponse(responseCode = "400", description = "Blank body", content = @Content),
      @ApiResponse(responseCode = "403", description = "Permission denied", content = @Content)
  })
  public ResponseEntity<UrlResponse> shortenUrl(@Valid @RequestBody UrlRequest request, Authentication authentication) {
    String username = authentication.getName();
    UrlResponse response = urlService.createShortUrl(request, username);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(summary = "Get all user URLs", description = "Returns a list of all user URLs")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of user URLs", content = { @Content(schema = @Schema(implementation = UrlResponse.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "403", description = "Permission denied", content = @Content),

  })
  public ResponseEntity<List<UrlResponse>> getUserUrls(Authentication authentication) {
    String username = authentication.getName();
    List<UrlResponse> urls = urlService.getUserUrls(username);
    return ResponseEntity.ok(urls);
  }

  @GetMapping("/{shortCode}")
  @Operation(summary = "Get a shortened URL", description = "Returns the original URL for the provided shortened URL")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Shortened URL", content = { @Content(schema = @Schema(implementation = UrlResponse.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Shortened URL not found", content = @Content)
  })
  public ResponseEntity<UrlResponse> getUrl(@PathVariable String shortCode, Authentication authentication) {
    String username = authentication.getName();
    UrlResponse url = urlService.getUrl(shortCode, username);
    return ResponseEntity.ok(url);
  }

  @GetMapping("/analytics/{shortCode}")
  @Operation(summary = "Get analytics for a shortened URL", description = "Returns analytics for the provided shortened URL")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Analytics", content = { @Content(schema = @Schema(implementation = AnalyticsResponse.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "404", description = "Shortened URL not found", content = @Content)
  })
  public ResponseEntity<AnalyticsResponse> getUrlAnalytics(@PathVariable String shortCode, Authentication authentication) {
    String username = authentication.getName();
    AnalyticsResponse analytics = analyticsService.getUrlAnalytics(shortCode, username);
    return ResponseEntity.ok(analytics);
  }

  @DeleteMapping("/{shortCode}")
  @Operation(summary = "Delete a shortened URL", description = "Deletes the shortened URL from the database")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Shortened URL deleted"),
      @ApiResponse(responseCode = "404", description = "Shortened URL not found", content = @Content),
      @ApiResponse(responseCode = "403", description = "Permission denied", content = @Content)
  })
  public ResponseEntity<Void> deleteUrl(@PathVariable String shortCode, Authentication authentication) {
    String username = authentication.getName();
    urlService.deleteUrl(shortCode, username);
    return ResponseEntity.noContent().build();
  }
}
