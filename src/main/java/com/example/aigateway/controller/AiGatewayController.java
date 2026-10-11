package com.example.aigateway.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aigateway.client.OllamaClient;
import com.example.aigateway.security.AiSecurityService;

/**
 * REST controller that exposes the AI Security Gateway endpoint.
 *
 * <p>Flow: receive prompt → sanitise → forward to Ollama → return response.</p>
 *
 * <p>Exception handling is delegated to
 * {@link com.example.aigateway.exception.GlobalExceptionHandler}.</p>
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiGatewayController {

    private static final Logger log = LoggerFactory.getLogger(AiGatewayController.class);

    private final AiSecurityService securityService;
    private final OllamaClient ollamaClient;

    public AiGatewayController(AiSecurityService securityService, OllamaClient ollamaClient) {
        this.securityService = securityService;
        this.ollamaClient = ollamaClient;
    }

    /**
     * Accepts a JSON body like {@code {"prompt": "Tell me about Java 25"}}
     * and returns either a successful AI response or a 403 block notice.
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String originalPrompt = request.get("prompt");

        // 1. Sanitize (throws PromptInjectionException if blocked — handled globally)
        String sanitizedPrompt = securityService.sanitize(originalPrompt);

        // 2. Query AI
        String aiResponse = ollamaClient.generate(sanitizedPrompt);
        log.info("AI response received ({} chars)", aiResponse.length());

        // 3. Return success
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "original_prompt", originalPrompt,
                "sanitized_prompt", sanitizedPrompt,
                "ai_response", aiResponse
        ));
    }
}
