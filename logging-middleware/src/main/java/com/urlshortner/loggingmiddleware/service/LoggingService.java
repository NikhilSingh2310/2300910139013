package com.urlshortner.loggingmiddleware.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LoggingService {

    private final WebClient webClient;
    private final String accessToken;

    public LoggingService(WebClient.Builder webClientBuilder, @Value("${app.access-token}") String accessToken) {
        this.webClient = webClientBuilder.baseUrl("http://20.244.56.144/evaluation-service").build();
        this.accessToken = accessToken;
    }

    public void log(String stack, String level, String packageName, String message) {
        Map<String, String> logPayload = Map.of(
                "stack", stack,
                "level", level,
                "package", packageName,
                "message", message
        );

        webClient.post()
                .uri("/logs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(logPayload), Map.class)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        response -> System.out.println("Log created successfully"),
                        error -> System.err.println("Error creating log: " + error.getMessage())
                );
    }
}
