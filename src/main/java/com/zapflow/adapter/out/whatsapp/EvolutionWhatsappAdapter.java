package com.zapflow.adapter.out.whatsapp;

import com.zapflow.adapter.out.whatsapp.dto.EvolutionSendResponse;
import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.SentMessage;
import com.zapflow.application.model.WhatsappProviderException;
import com.zapflow.application.port.out.SendMessagePort;
import com.zapflow.configuration.EvolutionProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static com.zapflow.application.model.WhatsappProviderException.FailureType.*;

public class EvolutionWhatsappAdapter implements SendMessagePort {
    private final RestClient client;
    private final EvolutionProperties properties;

    public EvolutionWhatsappAdapter(RestClient.Builder builder, EvolutionProperties properties) {
        this.properties = properties;
        client = builder.baseUrl(properties.baseUrl())
                .defaultStatusHandler(status -> status.isError(), (request, response) -> {
                    int status = response.getStatusCode().value();
                    var type = status == 401 || status == 403 || status == 404 ? CONFIGURATION
                            : status == 429 || status >= 500 ? TEMPORARY : PERMANENT;
                    String message = switch (type) {
                        case CONFIGURATION -> "Check the Evolution URL, instance name and API key";
                        case TEMPORARY -> "Evolution is temporarily unavailable or rate limited";
                        case PERMANENT -> "Evolution rejected the request; check the recipient and message data";
                    };
                    throw new WhatsappProviderException(type, status, message);
                }).build();
    }

    @Override
    public SentMessage sendMessage(SendMessageCommand command) {
        if (properties.instanceName() == null || properties.instanceName().isBlank()
                || properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new WhatsappProviderException(CONFIGURATION, null,
                    "Configure WHATSAPP_PROVIDER_INSTANCE and WHATSAPP_PROVIDER_API_KEY");
        }
        try {
            var response = client.post().uri("/message/sendText/{instance}", properties.instanceName())
                    .contentType(MediaType.APPLICATION_JSON).header("apikey", properties.apiKey())
                    .body(toPayload(command)).retrieve().body(EvolutionSendResponse.class);
            if (response == null || response.key() == null || response.key().id() == null
                    || response.key().id().isBlank()) throw invalidResponse();
            return new SentMessage(response.key().id(), response.status(), response.key().remoteJid(),
                    Boolean.TRUE.equals(response.key().fromMe()), command.text());
        } catch (ResourceAccessException exception) {
            throw new WhatsappProviderException(TEMPORARY, null,
                    "Could not reach Evolution; delivery may be unknown. Check before resending");
        } catch (RestClientException exception) {
            throw invalidResponse();
        }
    }

    private Map<String, Object> toPayload(SendMessageCommand command) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("number", command.number());
        payload.put("text", command.text());
        if (command.delay() != null) payload.put("delay", command.delay());
        if (command.linkPreview() != null) payload.put("linkPreview", command.linkPreview());
        if (!command.mentioned().isEmpty()) payload.put("mentioned", command.mentioned());
        if (!command.quoted().isEmpty()) payload.put("quoted", command.quoted());
        return payload;
    }

    private WhatsappProviderException invalidResponse() {
        return new WhatsappProviderException(PERMANENT, null, "Evolution returned an invalid message response");
    }
}
