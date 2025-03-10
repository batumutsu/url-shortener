package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.regex.Pattern;

@Component
public class UrlValidator {

  private static final Pattern URL_PATTERN = Pattern.compile(
      "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

  public boolean isValidUrl(String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }

    // Check format using regex
    if (!URL_PATTERN.matcher(url).matches()) {
      return false;
    }

    // Try to create URL object to validate
    try {
      new URL(url);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
