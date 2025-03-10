package com.urlshortener.service;

import com.urlshortener.dto.AnalyticsResponse;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.UnauthorizedException;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

  private final UrlRepository urlRepository;
  private final UserRepository userRepository;
  private final ClickAnalyticsRepository clickAnalyticsRepository;

  public AnalyticsService(UrlRepository urlRepository, UserRepository userRepository, ClickAnalyticsRepository clickAnalyticsRepository) {
    this.urlRepository = urlRepository;
    this.userRepository = userRepository;
    this.clickAnalyticsRepository = clickAnalyticsRepository;
  }

  @Value("${url.short.domain}")
  private String shortDomain;

  @Transactional(readOnly = true)
  public AnalyticsResponse getUrlAnalytics(String shortCode, String username) {
    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!url.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("You don't have permission to view analytics for this URL");
    }

    List<ClickAnalytics> clicksList = clickAnalyticsRepository.findByUrl(url);

    // Group clicks by day
    Map<String, Long> clicksByDay = clicksList.stream()
        .collect(Collectors.groupingBy(
            click -> click.getClickedAt().toLocalDate().format(DateTimeFormatter.ISO_DATE),
            Collectors.counting()
        ));

    // Group by referrer
    Map<String, Long> referrerCounts = clicksList.stream()
        .filter(click -> click.getReferrer() != null && !click.getReferrer().isEmpty())
        .collect(Collectors.groupingBy(
            this::extractDomain,
            Collectors.counting()
        ));

    // Group by browser
    Map<String, Long> browserCounts = clicksList.stream()
        .filter(click -> click.getUserAgent() != null && !click.getUserAgent().isEmpty())
        .collect(Collectors.groupingBy(
            this::extractBrowser,
            Collectors.counting()
        ));

    return AnalyticsResponse.builder()
        .urlId(url.getId())
        .shortCode(url.getShortCode())
        .shortUrl(shortDomain + "/" + url.getShortCode())
        .longUrl(url.getLongUrl())
        .totalClicks(url.getClicks())
        .clicksByDay(clicksByDay)
        .referrerCounts(referrerCounts)
        .browserCounts(browserCounts)
        .build();
  }

  private String extractDomain(ClickAnalytics click) {
    String referrer = click.getReferrer();
    if (referrer == null || referrer.isEmpty()) {
      return "Direct";
    }

    try {
      Pattern pattern = Pattern.compile("^(?:https?://)?(?:www\\.)?([^:/\\n?]+)");
      Matcher matcher = pattern.matcher(referrer);
      return matcher.find() ? matcher.group(1) : "Unknown";
    } catch (Exception e) {
      return "Unknown";
    }
  }

  private String extractBrowser(ClickAnalytics click) {
    String userAgent = click.getUserAgent();
    if (userAgent == null || userAgent.isEmpty()) {
      return "Unknown";
    }

    if (userAgent.contains("Chrome") && !userAgent.contains("Chromium")) {
      return "Chrome";
    } else if (userAgent.contains("Firefox")) {
      return "Firefox";
    } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
      return "Safari";
    } else if (userAgent.contains("Edge")) {
      return "Edge";
    } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
      return "Internet Explorer";
    } else {
      return "Other";
    }
  }
}
