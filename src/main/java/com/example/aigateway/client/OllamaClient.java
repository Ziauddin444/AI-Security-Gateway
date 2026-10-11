package com.example.aigateway.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that forwards sanitized prompts to a local Ollama instance
 * and returns the generated text response.
 */
@Component
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String model;

    public OllamaClient(RestTemplate restTemplate,
                         @Value("${ollama.base-url}") String baseUrl,
                         @Value("${ollama.model}") String model) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * Sends a prompt to the Ollama /api/generate endpoint and returns the
     * AI-generated response text.
     *
     * @param prompt the sanitized user prompt
     * @return the "response" field from the Ollama JSON reply
     */
    public String generate(String prompt) {
        String url = baseUrl + "/api/generate";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the request payload — stream: false so we get a single JSON object back
        Map<String, Object> payload = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);

        Map<?, ?> body = responseEntity.getBody();
        if (body == null || !body.containsKey("response")) {
            throw new RuntimeException("Unexpected response from Ollama — no 'response' field present");
        }

        return body.get("response").toString();
    }
}

