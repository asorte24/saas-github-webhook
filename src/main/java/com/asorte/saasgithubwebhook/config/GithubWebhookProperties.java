package com.asorte.saasgithubwebhook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.webhook")
public class GithubWebhookProperties {

    private String secret = "";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isSecretConfigured() {
        return secret != null && !secret.isBlank();
    }
}
