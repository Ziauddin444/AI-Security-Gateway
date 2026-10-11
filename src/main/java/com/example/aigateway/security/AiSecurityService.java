package com.example.aigateway.security;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Performs two-phase sanitisation on user prompts:
 * <ol>
 *   <li><strong>Injection detection</strong> — blocks prompts that contain known
 *       jailbreak / prompt-injection phrases.</li>
 *   <li><strong>PII redaction</strong> — replaces email addresses and phone numbers
 *       with placeholder tokens.</li>
 * </ol>
 */
@Service
public class AiSecurityService {

    private static final Logger log = LoggerFactory.getLogger(AiSecurityService.class);

    // ── Prompt-injection patterns (case-insensitive) ────────────────────────────

    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("ignore\\s+previous\\s+instructions", Pattern.CASE_INSENSITIVE),
            Pattern.compile("disregard\\s+above",                 Pattern.CASE_INSENSITIVE),
            Pattern.compile("you\\s+are\\s+now",                  Pattern.CASE_INSENSITIVE),
            Pattern.compile("system\\s+prompt",                   Pattern.CASE_INSENSITIVE),
            Pattern.compile("jailbreak",                          Pattern.CASE_INSENSITIVE),
            Pattern.compile("do\\s+anything\\s+now",              Pattern.CASE_INSENSITIVE)
    );

    // ── PII patterns ────────────────────────────────────────────────────────────

    /** RFC-5322-ish email pattern — good enough for redaction purposes. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}");

    /**
     * Matches common phone formats:
     *   +1-234-567-8901, (234) 567-8901, 234-567-8901,
     *   234.567.8901, 234 567 8901, +61 412 345 678, etc.
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[\\s.\\-]?)?(\\(?\\d{2,4}\\)?[\\s.\\-]?)?\\d{3,4}[\\s.\\-]?\\d{3,4}");

    // ── Public API ──────────────────────────────────────────────────────────────

    /**
     * Validates and sanitises the incoming prompt.
     *
     * @param rawPrompt the unprocessed user input
     * @return the sanitised prompt with PII redacted
     * @throws RuntimeException if a prompt-injection attempt is detected
     */
    public String sanitize(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            throw new RuntimeException("Prompt must not be empty");
        }

        // Phase 1 — check for injection attacks
        detectInjection(rawPrompt);

        // Phase 2 — redact PII
        String sanitized = redactPii(rawPrompt);

        log.info("Prompt sanitised successfully. Original length={}, sanitised length={}",
                rawPrompt.length(), sanitized.length());

        return sanitized;
    }

    // ── Internal helpers ────────────────────────────────────────────────────────

    private void detectInjection(String prompt) {
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                String matched = pattern.pattern().replace("\\s+", " ");
                log.warn("Prompt injection detected — matched pattern: {}", matched);
                throw new RuntimeException(
                        "Prompt injection detected: input matches forbidden pattern [" + matched + "]"
                );
            }
        }
    }

    private String redactPii(String prompt) {
        String result = EMAIL_PATTERN.matcher(prompt).replaceAll("[EMAIL_REDACTED]");
        result = PHONE_PATTERN.matcher(result).replaceAll("[PHONE_REDACTED]");
        return result;
    }
}

