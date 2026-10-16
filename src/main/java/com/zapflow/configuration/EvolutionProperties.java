package com.zapflow.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zapflow.provider")
public record EvolutionProperties(String baseUrl, String instanceName, String apiKey) {
}
