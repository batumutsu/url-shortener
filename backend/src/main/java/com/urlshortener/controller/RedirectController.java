package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class RedirectController {

  private final UrlService urlService;

  public RedirectController(UrlService urlService) {
    this.urlService = urlService;
  }

  @GetMapping("/{shortCode}")
  public RedirectView redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    String referrer = request.getHeader("Referer");
    String ipAddress = request.getRemoteAddr();

    String longUrl = urlService.getLongUrlAndIncrementClicks(shortCode, referrer, userAgent, ipAddress);

    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(longUrl);
    redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
    return redirectView;
  }
}
