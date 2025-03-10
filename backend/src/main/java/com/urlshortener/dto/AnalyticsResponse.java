package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse implements Serializable {
  private UUID urlId;
  private String shortCode;
  private String shortUrl;
  private String longUrl;
  private Integer totalClicks;
  private Map<String, Long> clicksByDay;
  private Map<String, Long> referrerCounts;
  private Map<String, Long> browserCounts;
}
