package com.zapflow.adapter.out.whatsapp;

import com.zapflow.application.model.SendMessageCommand;
import com.zapflow.application.model.WhatsappProviderException;
import com.zapflow.configuration.EvolutionProperties;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EvolutionWhatsappAdapterTests {
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final EvolutionWhatsappAdapter adapter = new EvolutionWhatsappAdapter(builder,
            new EvolutionProperties("http://evolution.test", "ZapFlow", "test-key"));
    private final String endpoint = "http://evolution.test/message/sendText/ZapFlow";

    private SendMessageCommand command() {
        return new SendMessageCommand("5511999999999", "Hello!", null, null, null, null);
    }

    @Test
    void sendsTextWithApiKeyAndMapsProviderResponse() {
        server.expect(requestTo(endpoint)).andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"number\":\"5511999999999\",\"text\":\"Hello!\"}", JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {"key":{"id":"ABC","remoteJid":"5511999999999@s.whatsapp.net","fromMe":true},
                         "message":{"conversation":"Hello!"},"status":"PENDING","messageTimestamp":123}
                        """, MediaType.APPLICATION_JSON));
        var result = adapter.sendMessage(command());
        assertEquals("ABC", result.messageId());
        assertEquals("PENDING", result.status());
        assertEquals("5511999999999@s.whatsapp.net", result.recipient());
        assertTrue(result.fromMe());
        server.verify();
    }

    @Test
    void forwardsOptionalFieldsAndKeepsFalsePreview() {
        var command = new SendMessageCommand("5511999999999", "Hello!", 123,
                Map.of("key", Map.of("id", "quoted-id")), false, List.of("5511888888888@s.whatsapp.net"));
        server.expect(requestTo(endpoint)).andExpect(content().json("""
                {"number":"5511999999999","text":"Hello!","delay":123,"linkPreview":false,
                 "quoted":{"key":{"id":"quoted-id"}},"mentioned":["5511888888888@s.whatsapp.net"]}
                """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("{\"key\":{\"id\":\"ABC\"},\"status\":\"PENDING\"}", MediaType.APPLICATION_JSON));
        adapter.sendMessage(command);
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"400,PERMANENT", "401,CONFIGURATION", "403,CONFIGURATION", "404,CONFIGURATION",
            "429,TEMPORARY", "500,TEMPORARY", "503,TEMPORARY"})
    void classifiesEvolutionHttpFailures(int status, WhatsappProviderException.FailureType expected) {
        server.expect(requestTo(endpoint)).andRespond(withStatus(HttpStatus.valueOf(status))
                .contentType(MediaType.APPLICATION_JSON).body("{\"message\":\"private test-key\"}"));
        var error = assertThrows(WhatsappProviderException.class, () -> adapter.sendMessage(command()));
        assertEquals(expected, error.getFailureType());
        assertEquals(status, error.getProviderCode());
        assertFalse(error.getMessage().contains("test-key"));
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"key\":{}}", "{\"key\":{\"id\":\"\"}}", "invalid JSON", ""})
    void rejectsInvalidSuccessResponses(String body) {
        server.expect(requestTo(endpoint)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThrows(WhatsappProviderException.class, () -> adapter.sendMessage(command()));
        server.verify();
    }

    @Test
    void reportsUnknownDeliveryOnTimeoutWithoutRetrying() {
        server.expect(requestTo(endpoint)).andRespond(withException(new IOException("read timeout")));
        var error = assertThrows(WhatsappProviderException.class, () -> adapter.sendMessage(command()));
        assertEquals(WhatsappProviderException.FailureType.TEMPORARY, error.getFailureType());
        assertTrue(error.getMessage().contains("delivery may be unknown"));
        server.verify();
    }

    @Test
    void rejectsMissingConfigurationBeforeHttpCall() {
        var missing = new EvolutionWhatsappAdapter(builder, new EvolutionProperties("http://evolution.test", "", ""));
        assertEquals(WhatsappProviderException.FailureType.CONFIGURATION,
                assertThrows(WhatsappProviderException.class, () -> missing.sendMessage(command())).getFailureType());
        server.verify();
    }
}
