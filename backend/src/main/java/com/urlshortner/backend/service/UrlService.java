package com.urlshortner.backend.service;

import com.urlshortner.backend.dto.UrlRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlService {

    private final Map<String, String> urlMap = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    public String shortenUrl(UrlRequest request) {
        String longUrl = request.getUrl();
        String shortCode;

        if (request.getCustomShortcode() != null && !request.getCustomShortcode().isEmpty()) {
            shortCode = request.getCustomShortcode();
            if (urlMap.containsKey(shortCode)) {
                throw new IllegalArgumentException("Custom shortcode is already in use.");
            }
        } else {
            do {
                shortCode = generateRandomShortCode();
            } while (urlMap.containsKey(shortCode));
        }

        urlMap.put(shortCode, longUrl);
        return shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        String originalUrl = urlMap.get(shortCode);
        if (originalUrl == null) {
            throw new IllegalArgumentException("Shortcode not found.");
        }
        return originalUrl;
    }

    private String generateRandomShortCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}