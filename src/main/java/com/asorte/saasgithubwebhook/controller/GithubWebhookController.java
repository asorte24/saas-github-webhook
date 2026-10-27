package com.asorte.saasgithubwebhook.service;

import com.asorte.saasgithubwebhook.config.GithubWebhookProperties;
import com.asorte.saasgithubwebhook.model.WebhookNotification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class GithubWebhookService {

    private static final Logger log = LoggerFactory.getLogger(GithubWebhookService.class);

    private final GithubWebhookProperties properties;
    private final ObjectMapper objectMapper;

    public GithubWebhookService(GithubWebhookProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void logStartup() {
        log.info("GitHub webhook receiver is active. Secret configured: {}", properties.isSecretConfigured());
    }

    public WebhookNotification processWebhook(String eventName, String payload, String signature, String deliveryId) {
        if (eventName == null || eventName.isBlank()) {
            throw new IllegalArgumentException("GitHub event header is required");
        }

        if (properties.isSecretConfigured() && (signature == null || signature.isBlank())) {
            throw new SecurityException("Missing X-Hub-Signature-256 header");
        }

        if (properties.isSecretConfigured()) {
            validateSignature(payload, signature);
        }

        JsonNode root = parsePayload(payload);
        String action = root.path("action").asText();
        String organization = root.path("organization").path("login").asText();
        String repository = root.path("repository").path("full_name").asText();
        String summary = buildSummary(eventName, action, organization, repository);

        log.info("Received GitHub {} event for organization={} repository={} action={} deliveryId={}",
                eventName, organization, repository, action, deliveryId);

        return new WebhookNotification(
                eventName,
                action,
                organization,
                repository,
                deliveryId,
                summary,
                Instant.now()
        );
    }

    private void validateSignature(String payload, String signature) {
        String expectedAlgorithm = "sha256=";
        if (signature == null || !signature.startsWith(expectedAlgorithm)) {
            throw new SecurityException("Invalid signature format. Expected sha256=... header");
        }

        String computedSignature = expectedAlgorithm + calculateHmacSha256(payload, properties.getSecret());
        if (!MessageDigest.isEqual(computedSignature.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Signature validation failed");
        }
    }

    private String calculateHmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to calculate HMAC-SHA256 signature", e);
        }
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse GitHub payload as JSON", e);
        }
    }

    private String buildSummary(String eventName, String action, String organization, String repository) {
        StringBuilder summary = new StringBuilder();
        summary.append("GitHub event '").append(eventName).append("'");
        if (action != null && !action.isBlank()) {
            summary.append(" action='").append(action).append("'");
        }
        if (organization != null && !organization.isBlank()) {
            summary.append(" organization='").append(organization).append("'");
        }
        if (repository != null && !repository.isBlank()) {
            summary.append(" repository='").append(repository).append("'");
        }
        return summary.toString();
    }
}
