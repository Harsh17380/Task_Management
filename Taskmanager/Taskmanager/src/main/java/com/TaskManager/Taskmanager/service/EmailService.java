package com.TaskManager.Taskmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key:}")
    private String apiKey;

    @Value("${brevo.sender.email:}")
    private String senderEmail;

    @Value("${brevo.sender.name:CoreQueue}")
    private String senderName;

    private final RestTemplate restTemplate;

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isMailConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && senderEmail != null && !senderEmail.isBlank();
    }

    public void sendEmail(String to, String subject, String body) {

        String url = "https://api.brevo.com/v3/smtp/email";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", apiKey);

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of("email", to)
                ),
                "subject", subject,
                "textContent", body
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, request, String.class);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Brevo email request failed: " + ex.getMessage(), ex);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Brevo Email Failed : " + response.getBody());
        }
    }
}
