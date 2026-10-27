package com.asorte.saasgithubwebhook.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record WebhookNotification(
        String event,
        String action,
        String organization,
        String repository,
        String deliveryId,
        String summary,
        Instant receivedAt
) {
    public Map<String, Object> toMap() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("action", action);
        payload.put("organization", organization);
        payload.put("repository", repository);
        payload.put("deliveryId", deliveryId);
        payload.put("summary", summary);
        payload.put("receivedAt", receivedAt.toString());
        return payload;
    }
}
