package com.example.aigateway.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

        if (originalPrompt == null || originalPrompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "reason", "Request body must contain a non-empty 'prompt' field"
            ));
        }

        // ── Phase 1: Sanitise ───────────────────────────────────────────────
        String sanitizedPrompt;
        try {
            sanitizedPrompt = securityService.sanitize(originalPrompt);
        } catch (RuntimeException ex) {
            log.warn("Prompt blocked: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "status", "BLOCKED",
                    "reason", ex.getMessage()
            ));
        }

        // ── Phase 2: Forward to Ollama ──────────────────────────────────────
        try {
            String aiResponse = ollamaClient.generate(sanitizedPrompt);
            log.info("AI response received ({} chars)", aiResponse.length());

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "original_prompt", originalPrompt,
                    "sanitized_prompt", sanitizedPrompt,
                    "ai_response", aiResponse
            ));
        } catch (Exception ex) {
            log.error("Ollama call failed", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "ERROR",
                    "reason", "AI backend unavailable: " + ex.getMessage()
            ));
        }
    }
}

