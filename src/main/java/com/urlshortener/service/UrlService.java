package com.urlshortener.service;


import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.exception.BadRequestException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.User;
import com.urlshortener.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UrlService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)[\\w.-]+(:\\d+)?(/.*)?$");

    private final UrlMappingRepository urlMappingRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public UrlResponse shorten(ShortenRequest request, User owner) {
        String originalUrl = request.getOriginalUrl().trim();

        if (!URL_PATTERN.matcher(originalUrl).matches()) {
            throw new BadRequestException("originalUrl must be a valid http/https URL");
        }

        String shortCode;
        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            shortCode = request.getCustomCode().trim();
            if (urlMappingRepository.existsByShortCode(shortCode)) {
                throw new BadRequestException("Custom code already in use, please choose another one");
            }
        } else {
            shortCode = generateUniqueCode();
        }

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .clickCount(0L)
                .expiryDate(request.getExpiryDate())
                .owner(owner)
                .build();

        urlMappingRepository.save(mapping);
        return toResponse(mapping);
    }

    public List<UrlResponse> getUrlsForUser(User owner) {
        return urlMappingRepository.findByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public String resolveAndRegisterClick(String shortCode) {
        UrlMapping mapping = urlMappingRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        if (mapping.isExpired()) {
            throw new ResourceNotFoundException("This short URL has expired");
        }

        mapping.setClickCount(mapping.getClickCount() + 1);
        urlMappingRepository.save(mapping);

        return mapping.getOriginalUrl();
    }

    @Transactional
    public void deleteUrl(Long id, User owner) {
        UrlMapping mapping = urlMappingRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found or you do not own it"));
        urlMappingRepository.delete(mapping);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (urlMappingRepository.existsByShortCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private UrlResponse toResponse(UrlMapping mapping) {
        return UrlResponse.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + mapping.getShortCode())
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .expiryDate(mapping.getExpiryDate())
                .build();
    }
}