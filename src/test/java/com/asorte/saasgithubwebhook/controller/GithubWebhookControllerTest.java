package com.asorte.saasgithubwebhook.controller;

import com.asorte.saasgithubwebhook.model.WebhookNotification;
import com.asorte.saasgithubwebhook.service.GithubWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GithubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GithubWebhookController.class);

    private final GithubWebhookService githubWebhookService;

    public GithubWebhookController(GithubWebhookService githubWebhookService) {
        this.githubWebhookService = githubWebhookService;
    }

    @PostMapping("/github-webhook")
    public ResponseEntity<Map<String, Object>> receiveGithubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventName,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody String payload) {

        try {
            WebhookNotification notification = githubWebhookService.processWebhook(eventName, payload, signature, deliveryId);
            return ResponseEntity.accepted().body(notification.toMap());
        } catch (SecurityException e) {
            log.warn("Validation failed for GitHub webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Bad request to GitHub webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to process GitHub webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unable to process webhook"));
        }
    }
}
