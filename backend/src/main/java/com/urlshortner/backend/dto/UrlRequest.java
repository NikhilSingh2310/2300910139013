package com.urlshortner.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UrlRequest {
    @NotBlank(message = "URL cannot be blank.")
    @URL(message = "URL must be a valid format.")
    private String url;
    private String customShortcode;
}
