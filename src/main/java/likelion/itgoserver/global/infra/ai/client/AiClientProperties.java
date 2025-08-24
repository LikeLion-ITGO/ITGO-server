package likelion.itgoserver.global.infra.ai.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiClientProperties(
        String baseUrl
) {}