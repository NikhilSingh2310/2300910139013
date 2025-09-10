package com.urlshortner.backend.controller;

import com.urlshortner.loggingmiddleware.service.LoggingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import org.hibernate.validator.constraints.URL;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class UrlController {

    @Autowired
    private LoggingService loggingService;

    // In-memory storage for URL mappings
    private final Map<String, String> urlMap = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    @PostMapping("/shortUrls")
    public ResponseEntity<String> shortenUrl(@Valid @RequestBody UrlRequest request) {
        loggingService.log("backend", "info", "UrlController", "Received request to shorten URL: " + request.getUrl());

        String longUrl = request.getUrl();
        String shortCode;

        if (request.getCustomShortcode() != null && !request.getCustomShortcode().isEmpty()) {
            shortCode = request.getCustomShortcode();
            if (urlMap.containsKey(shortCode)) {
                loggingService.log("backend", "error", "UrlController", "Custom shortcode already exists: " + shortCode);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Custom shortcode already exists.");
            }
        } else {
            do {
                shortCode = generateRandomShortCode();
            } while (urlMap.containsKey(shortCode));
        }

        urlMap.put(shortCode, longUrl);
        loggingService.log("backend", "info", "UrlController", "URL shortened successfully. Short code: " + shortCode);

        String fullShortUrl = "http://your-host/" + shortCode;
        return ResponseEntity.ok(fullShortUrl);
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        loggingService.log("backend", "info", "UrlController", "Received redirect request for short code: " + shortCode);
        String originalUrl = urlMap.get(shortCode);

        if (originalUrl != null) {
            return new RedirectView(originalUrl);
        } else {
            loggingService.log("backend", "error", "UrlController", "Short code not found: " + shortCode);
            return new RedirectView("/error");
        }
    }

    private String generateRandomShortCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    // A simple DTO-like class as a method parameter
    static class UrlRequest {
        @NotBlank(message = "URL cannot be blank.")
        @URL(message = "URL must be a valid format.")
        private String url;
        private String customShortcode;

        // Getters and setters (omitted for brevity, but needed)
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getCustomShortcode() { return customShortcode; }
        public void setCustomShortcode(String customShortcode) { this.customShortcode = customShortcode; }
    }
}