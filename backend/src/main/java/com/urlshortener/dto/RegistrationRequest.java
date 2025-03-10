package com.urlshortener.dto;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest implements Serializable {

  @NotBlank(message = "Username is required")
  @Size(min = 6, max = 12, message = "Username must be between 6 and 12 characters")
  @Schema(
      description = "Username of the user to be registered",
      example = "JohnDoe",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Username is required")
  @Length(min = 6, max = 12, message = "Username must be greater than 6 and less than 12 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9]{6,12}$", message = "Username must be of 6 to 12 length with no special characters"
  )
  private String username;

  @Schema(
      description = "Email address of the user to be registered",
      example = "example@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Email is required")
  @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "Please provide a valid email address"
  )
  @Length(min = 5, max = 100, message = "Email must be between 5 and 100 characters")
  private String email;

  @Schema(
      description = "Password for the user account",
      example = "StrongP@ss123",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Password is required")
  @Length(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()]).{8,}$",
      message = "Password must contain at least one digit, one lowercase, one uppercase, " +
          "and one special character (@#$%^&+=!*())"
  )
  private String password;
}
