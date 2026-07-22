package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {

    @NotBlank(message = "originalUrl must not be blank")
    private String originalUrl;

    // optional: user can request a custom short code (e.g. "my-brand")
    private String customCode;

    // optional: expiry date for the short link
    private LocalDateTime expiryDate;
}