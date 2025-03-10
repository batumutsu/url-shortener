package com.urlshortener.dto;

import java.io.Serializable;

import org.hibernate.validator.constraints.URL;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Schema(description = "Shortened URL request body")
@Builder
public class UrlRequest implements Serializable {

  @NotEmpty(message = "Original URL is required")
  @URL(message = "Invalid URL")
  @Schema(description = "Original URL", example = "https://www.example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String longUrl;
}
