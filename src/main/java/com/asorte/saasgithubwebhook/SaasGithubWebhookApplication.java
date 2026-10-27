package com.asorte.saasgithubwebhook;

import com.asorte.saasgithubwebhook.config.GithubWebhookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GithubWebhookProperties.class)
public class SaasGithubWebhookApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasGithubWebhookApplication.class, args);
    }
}
