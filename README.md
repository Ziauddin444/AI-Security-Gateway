# 🛡️ AI Security Gateway

An enterprise-grade, Spring Boot-based API gateway designed to secure Large Language Model (LLM) interactions. It acts as a protective firewall between client applications and AI backends (like Ollama), preventing data leaks and adversarial attacks.

## 🚨 The Problem It Solves
As enterprises rapidly integrate LLMs into their Java backends, they face critical security risks:
1. **PII Leakage:** Users accidentally sending emails, phone numbers, or sensitive data to third-party AI models.
2. **Prompt Injection:** Malicious actors using techniques like "Ignore previous instructions" to jailbreak the model or extract system prompts.

This gateway intercepts, sanitizes, and validates all prompts *before* they ever reach the AI.

## 🏗️ Architecture

```mermaid
sequenceDiagram
    participant Client as Client App
    participant Gateway as AI Security Gateway (Spring Boot)
    participant AI as Local LLM (Ollama)

    Client->>Gateway: POST /api/v1/ai/chat (Raw Prompt)
    Gateway->>Gateway: 1. Check for Prompt Injection (Regex)
    Gateway->>Gateway: 2. Redact PII (Emails, Phones)
    alt Injection Detected
        Gateway-->>Client: 403 Forbidden (BLOCKED)
    else Safe Prompt
        Gateway->>AI: POST /api/generate (Sanitized Prompt)
        AI-->>Gateway: AI Response
        Gateway-->>Client: 200 OK (Success + Sanitized Prompt)
    end
```

✨ Key Features
OWASP LLM Top 10 Mitigation: Blocks common prompt injection patterns (e.g., "jailbreak", "ignore previous instructions", "system prompt").
Automated PII Redaction: Uses Regex to automatically mask emails and phone numbers with [EMAIL_REDACTED] and [PHONE_REDACTED].
Global Exception Handling: Returns clean, predictable JSON error responses (403 Forbidden) instead of raw stack traces.
Zero External AI Dependencies: Routes traffic to a local, self-hosted Ollama instance for complete data privacy.
🛠️ Tech Stack
Language: Java 21/25
Framework: Spring Boot 3.4.x
Build Tool: Maven
AI Backend: Ollama (running llama3.2)
🚀 Getting Started
Prerequisites
Install Java 21+
    
