package com.zapflow.adapter.out.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Conexão com a Evolution API (ver .env.example).
 */
@ConfigurationProperties("zapflow.provider")
public record EvolutionProperties(String baseUrl, String instanceName, String apiKey) {
}
