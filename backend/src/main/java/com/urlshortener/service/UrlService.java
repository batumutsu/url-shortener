package com.urlshortener.service;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UnauthorizedException;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.util.ShortCodeGenerator;
import com.urlshortener.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UrlService {

  private final UrlRepository urlRepository;
  private final UserRepository userRepository;
  private final ClickAnalyticsRepository clickAnalyticsRepository;
  private final ShortCodeGenerator shortCodeGenerator;
  private final UrlValidator urlValidator;

  public UrlService(UrlRepository urlRepository, UserRepository userRepository, ClickAnalyticsRepository clickAnalyticsRepository, ShortCodeGenerator shortCodeGenerator, UrlValidator urlValidator) {
    this.urlRepository = urlRepository;
    this.userRepository = userRepository;
    this.clickAnalyticsRepository = clickAnalyticsRepository;
    this.shortCodeGenerator = shortCodeGenerator;
    this.urlValidator = urlValidator;
  }

  @Value("${url.short.domain}")
  private String shortDomain;

  @Value("${url.short.length}")
  private int shortCodeLength;

  @Transactional
  public UrlResponse createShortUrl(UrlRequest request, String username) {
    if (!urlValidator.isValidUrl(request.getLongUrl())) {
      throw new IllegalArgumentException("Invalid URL format");
    }

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Check if URL already exists
    Optional<Url> exists = urlRepository.findByLongUrlAndUser(request.getLongUrl(), user);

    if (exists.isPresent()) {
      return mapToUrlResponse(exists.get());
    }

    String shortCode;
      // Generate a unique short code
      do {
        shortCode = shortCodeGenerator.generate(shortCodeLength);
      } while (urlRepository.existsByShortCode(shortCode));


    Url url = Url.builder()
        .user(user)
        .shortCode(shortCode)
        .longUrl(request.getLongUrl())
        .clicks(0)
        .build();

    url = urlRepository.save(url);

    return mapToUrlResponse(url);
  }

  @Transactional(readOnly = true)
  public List<UrlResponse> getUserUrls(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return urlRepository.findByUser(user).stream()
        .map(this::mapToUrlResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public UrlResponse getUrl(String shortCode, String username) {
    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!url.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("You don't have permission to access this URL");
    }

    return mapToUrlResponse(url);
  }

  @Transactional
  public String getLongUrlAndIncrementClicks(String shortCode, String referrer, String userAgent, String ipAddress) {
    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

    // Increment click count
    urlRepository.incrementClicks(shortCode);

    // Record click analytics
    ClickAnalytics analytics = ClickAnalytics.builder()
        .url(url)
        .referrer(referrer)
        .userAgent(userAgent)
        .ipAddress(ipAddress)
        .build();
    clickAnalyticsRepository.save(analytics);

    return url.getLongUrl();
  }

  @Transactional
  public void deleteUrl(String shortCode, String username) {
    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!url.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("You don't have permission to delete this URL");
    }

    urlRepository.delete(url);
  }

  private UrlResponse mapToUrlResponse(Url url) {
    return UrlResponse.builder()
        .id(url.getId())
        .shortCode(url.getShortCode())
        .shortUrl(shortDomain + "/" + url.getShortCode())
        .longUrl(url.getLongUrl())
        .createdAt(url.getCreatedAt())
        .clicks(url.getClicks())
        .build();
  }
}


