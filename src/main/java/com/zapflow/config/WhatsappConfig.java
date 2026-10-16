package com.zapflow.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.zapflow.adapter.out.whatsapp.EvolutionProperties;
import com.zapflow.adapter.out.whatsapp.EvolutionWhatsappAdapter;

/**
 * Liga a porta de envio ({@code SendMessagePort}) ao adapter da Evolution.
 */
@Configuration
@EnableConfigurationProperties(EvolutionProperties.class)
public class WhatsappConfig {

    @Bean
    EvolutionWhatsappAdapter evolutionWhatsappAdapter(EvolutionProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return new EvolutionWhatsappAdapter(RestClient.builder().requestFactory(factory), properties);
    }
}
