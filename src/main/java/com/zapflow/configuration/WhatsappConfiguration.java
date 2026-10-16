package com.zapflow.configuration;

import com.zapflow.adapter.out.whatsapp.EvolutionWhatsappAdapter;
import com.zapflow.application.port.out.SendMessagePort;
import com.zapflow.application.service.SendMessageService;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(EvolutionProperties.class)
public class WhatsappConfiguration {
    @Bean
    EvolutionWhatsappAdapter evolutionWhatsappAdapter(EvolutionProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
        return new EvolutionWhatsappAdapter(RestClient.builder().requestFactory(factory), properties);
    }

    @Bean
    SendMessageService sendMessageService(SendMessagePort provider) {
        return new SendMessageService(provider);
    }
}
