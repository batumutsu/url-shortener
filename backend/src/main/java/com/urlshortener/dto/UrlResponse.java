package com.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Shortened URL response body")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlResponse implements Serializable {
  private UUID id;
  private String shortCode;
  private String shortUrl;
  private String longUrl;
  private LocalDateTime createdAt;
  private Integer clicks;
}
