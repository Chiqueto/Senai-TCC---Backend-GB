package com.senai.gestao_beneficios.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${azure.ai.key}") // Pode manter o nome da propriedade
    private String apiKey;

    @Value("${azure.ai.endpoint}")
    private String endpoint;

    @Bean("githubWebClient")
    public WebClient chatWebClient() {
        return WebClient.builder()
                .baseUrl(endpoint)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("huggingFaceWebClient")
    public WebClient huggingFaceWebClient(
            @Value("${huggingface.api.key}") String apiKey,
            @Value("${huggingface.api.endpoint}") String endpoint
    ) {
        return WebClient.builder()
                .baseUrl(endpoint)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
