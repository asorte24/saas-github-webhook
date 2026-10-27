package com.asorte.saasgithubwebhook.controller;

import com.asorte.saasgithubwebhook.config.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "github.webhook.secret=test-secret")
class GithubWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GithubWebhookProperties properties;

    @Test
    void shouldAcceptValidGitHubWebhook() throws Exception {
        String payload = "{\"action\":\"created\",\"organization\":{\"login\":\"acme\"},\"repository\":{\"full_name\":\"acme/sample-repo\"}}";
        String signature = createSignature(payload, properties.getSecret());

        mockMvc.perform(post("/api/github-webhook")
                        .header("X-GitHub-Event", "repository")
                        .header("X-GitHub-Delivery", "delivery-123")
                        .header("X-Hub-Signature-256", "sha256=" + signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted());
    }

    private String createSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
