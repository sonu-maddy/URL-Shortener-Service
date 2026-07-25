package com.urlshortener.controller;


import com.urlshortener.dto.ShortenRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.model.User;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;
    private final UserRepository userRepository;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shorten(@Valid @RequestBody ShortenRequest request,
                                               Authentication authentication) {
        User owner = currentUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shorten(request, owner));
    }

    @GetMapping("/my")
    public ResponseEntity<List<UrlResponse>> myUrls(Authentication authentication) {
        User owner = currentUser(authentication);
        return ResponseEntity.ok(urlService.getUrlsForUser(owner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id, Authentication authentication) {
        User owner = currentUser(authentication);
        urlService.deleteUrl(id, owner);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}