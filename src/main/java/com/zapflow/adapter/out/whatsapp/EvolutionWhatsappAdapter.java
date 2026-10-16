package com.zapflow.adapter.out.whatsapp;

import static com.zapflow.application.port.out.WhatsappProviderException.FailureType.CONFIGURATION;
import static com.zapflow.application.port.out.WhatsappProviderException.FailureType.PERMANENT;
import static com.zapflow.application.port.out.WhatsappProviderException.FailureType.TEMPORARY;

import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.zapflow.adapter.out.whatsapp.dto.EvolutionSendRequest;
import com.zapflow.adapter.out.whatsapp.dto.EvolutionSendResponse;
import com.zapflow.application.port.out.SendMessagePort;
import com.zapflow.application.port.out.WhatsappProviderException;
import com.zapflow.domain.contact.PhoneNumber;

/**
 * Envia mensagens pela Evolution API v2. Tudo que é formato da Evolution fica aqui dentro.
 */
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
                        case CONFIGURATION -> "Confira a URL, o nome da instância e a chave da Evolution";
                        case TEMPORARY -> "Evolution indisponível ou limitando envios no momento";
                        case PERMANENT -> "Evolution recusou o envio; confira o destinatário e a mensagem";
                    };
                    throw new WhatsappProviderException(type, status, message);
                }).build();
    }

    @Override
    public String sendText(PhoneNumber phone, String text) {
        if (properties.instanceName() == null || properties.instanceName().isBlank()
                || properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new WhatsappProviderException(CONFIGURATION, null,
                    "Configure WHATSAPP_PROVIDER_INSTANCE e WHATSAPP_PROVIDER_API_KEY");
        }
        try {
            var response = client.post().uri("/message/sendText/{instance}", properties.instanceName())
                    .contentType(MediaType.APPLICATION_JSON).header("apikey", properties.apiKey())
                    .body(new EvolutionSendRequest(toEvolutionNumber(phone), text))
                    .retrieve().body(EvolutionSendResponse.class);
            if (response == null || response.key() == null || response.key().id() == null
                    || response.key().id().isBlank()) {
                throw invalidResponse();
            }
            return response.key().id();
        } catch (ResourceAccessException exception) {
            throw new WhatsappProviderException(TEMPORARY, null,
                    "Sem resposta da Evolution; a mensagem pode ter sido entregue. Confira antes de reenviar");
        } catch (RestClientException exception) {
            throw invalidResponse();
        }
    }

    // A Evolution espera só os dígitos (5521999999999), sem o "+" do E.164
    private static String toEvolutionNumber(PhoneNumber phone) {
        return phone.value().substring(1);
    }

    private static WhatsappProviderException invalidResponse() {
        return new WhatsappProviderException(PERMANENT, null, "Evolution devolveu uma resposta inválida");
    }
}
